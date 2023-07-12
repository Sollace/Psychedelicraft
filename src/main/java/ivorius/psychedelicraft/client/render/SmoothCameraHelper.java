/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.util.math.Vec2f;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 23.02.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class SmoothCameraHelper {
    public static final SmoothCameraHelper INSTANCE = new SmoothCameraHelper();

    private final SmoothUtil xSmoother = new SmoothUtil();
    private final SmoothUtil ySmoother = new SmoothUtil();

    private float lastTickDelta;

    private Vec2f cursorDelta = new Vec2f(0, 0);

    private Vec2f smoothedCursor = new Vec2f(0, 0);
    private Vec2f prevCursorDelta = new Vec2f(0, 0);

    public void setCursorDelta(float deltaX, float deltaY) {
        prevCursorDelta = new Vec2f(deltaX, deltaY);
    }

    public void applyCameraChange() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.isWindowFocused() && !mc.isPaused() && mc.player != null) {
            DrugProperties properties = DrugProperties.of(mc.player);
            if (properties.getModifier(Drug.HEAD_MOTION_INERTNESS) > 0) {
                Vec2f angles = getAngles();

                if (!mc.options.smoothCameraEnabled) {
                    Vec2f o = getOriginalAngles();
                    angles = new Vec2f(angles.x - o.x, angles.y - o.y);
                }

                mc.player.changeLookDirection(angles.x, angles.y);
            }
        }
    }

    public void tick(DrugProperties properties) {
        float multiplier = MathHelper.clamp(properties.getModifier(Drug.HEAD_MOTION_INERTNESS), 0, 1);
        float speed = getSpeed();
        smoothedCursor = new Vec2f(
                (float)xSmoother.smooth(cursorDelta.x, multiplier * speed),
                (float)ySmoother.smooth(cursorDelta.y, multiplier * speed)
        );
        lastTickDelta = 0;
        cursorDelta = new Vec2f(0, 0);
    }

    private Vec2f getAngles() {
        float speed = getSpeed();
        cursorDelta = new Vec2f(cursorDelta.x + prevCursorDelta.x * speed, cursorDelta.y + prevCursorDelta.y * speed);

        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float progress = tickDelta - lastTickDelta;
        lastTickDelta = tickDelta;

        return new Vec2f(smoothedCursor.x * progress, smoothedCursor.y * progress * getYSignum());
    }

    private Vec2f getOriginalAngles() {
        float speed = getSpeed();
        return new Vec2f(prevCursorDelta.x * speed, prevCursorDelta.y * speed * getYSignum());
    }

    private float getYSignum() {
        return MinecraftClient.getInstance().options.getInvertYMouse().getValue() ? -1 : 1;
    }

    private float getSpeed() {
        float sensitivity = MinecraftClient.getInstance().options.getMouseSensitivity().getValue().floatValue() * 0.6F + 0.2F;
        sensitivity = sensitivity * sensitivity * sensitivity;
        if (!MinecraftClient.getInstance().player.isUsingSpyglass()) {
            sensitivity *= 8;
        }
        return sensitivity;
    }
}
