/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;

/**
 * Created by lukas on 23.02.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class SmoothCameraHelper {
    public static final SmoothCameraHelper INSTANCE = new SmoothCameraHelper();

    private final SmoothUtil xSmoother = new SmoothUtil();
    private final SmoothUtil ySmoother = new SmoothUtil();

    private float lastTickDelta;

    private float cursorDeltaX;
    private float cursorDeltaY;

    private float smoothedCursorX;
    private float smoothedCursorY;

    public void update(float multiplier) {
        float speed = getSpeed();
        smoothedCursorX = (float)this.xSmoother.smooth(cursorDeltaX, multiplier * speed);
        smoothedCursorY = (float)this.ySmoother.smooth(cursorDeltaY, multiplier * speed);
        lastTickDelta = 0;
        cursorDeltaX = 0;
        cursorDeltaY = 0;
    }

    public float[] getAngles(float deltaX, float deltaY) {
        float speed = getSpeed();
        this.cursorDeltaX += (deltaX * speed);
        this.cursorDeltaY += (deltaY * speed);

        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float progress = tickDelta - lastTickDelta;
        lastTickDelta = tickDelta;
        return new float[]{
            smoothedCursorX * progress,
            smoothedCursorY * progress * getYSignum()
        };
    }

    public float[] getOriginalAngles(float deltaX, float deltaY) {
        float speed = getSpeed();
        return new float[] {
            deltaX * speed,
            deltaY * speed * getYSignum()
        };
    }

    private float getYSignum() {
        return MinecraftClient.getInstance().options.getInvertYMouse().getValue() ? -1 : 1;
    }

    private float getSpeed() {
        float sensitivity = MinecraftClient.getInstance().options.getMouseSensitivity().getValue().floatValue() * 0.6F + 0.2F;
        return sensitivity * sensitivity * sensitivity * 8;
    }
}
