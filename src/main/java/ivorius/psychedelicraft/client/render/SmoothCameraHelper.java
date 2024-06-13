/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import org.joml.Vector2f;

import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Smoother;

/**
 * Created by lukas on 23.02.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class SmoothCameraHelper {
    public static final SmoothCameraHelper INSTANCE = new SmoothCameraHelper();

    private final Smoother xSmoother = new Smoother();
    private final Smoother ySmoother = new Smoother();

    private float lastTickDelta;

    private Vector2f cursorDelta = new Vector2f();

    private Vector2f smoothedCursor = new Vector2f();
    private Vector2f prevCursorDelta = new Vector2f();

    public void setCursorDelta(float deltaX, float deltaY) {
        prevCursorDelta.x = deltaX;
        prevCursorDelta.y = deltaY;
    }

    public void applyCameraChange() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.isWindowFocused() && !mc.isPaused() && mc.player != null) {
            DrugProperties properties = DrugProperties.of(mc.player);
            if (properties.getModifier(Drug.HEAD_MOTION_INERTNESS) > 0) {
                Vector2f angles = getAngles();

                if (!mc.options.smoothCameraEnabled) {
                    angles.sub(getOriginalAngles());
                }

                mc.player.changeLookDirection(angles.x, angles.y);
            }
        }
    }

    public void tick(DrugProperties properties) {
        float multiplier = MathHelper.clamp(properties.getModifier(Drug.HEAD_MOTION_INERTNESS), 0, 1);
        float speed = getSpeed();
        smoothedCursor.set(
                (float)xSmoother.smooth(cursorDelta.x, multiplier * speed),
                (float)ySmoother.smooth(cursorDelta.y, multiplier * speed)
        );
        lastTickDelta = 0;
        cursorDelta.set(0, 0);
    }

    private Vector2f getAngles() {
        float speed = getSpeed();
        cursorDelta.add(prevCursorDelta.x * speed, prevCursorDelta.y * speed);

        float tickDelta = ShaderContext.tickDelta();
        float progress = tickDelta - lastTickDelta;
        lastTickDelta = tickDelta;

        return smoothedCursor.mul(progress, progress * getYSignum(), new Vector2f());
    }

    private Vector2f getOriginalAngles() {
        float speed = getSpeed();
        return prevCursorDelta.mul(speed, speed * getYSignum(), new Vector2f());
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
