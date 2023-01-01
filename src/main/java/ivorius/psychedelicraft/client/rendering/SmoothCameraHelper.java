/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import net.minecraft.util.MouseFilter;

/**
 * Created by lukas on 23.02.14.
 */
public class SmoothCameraHelper
{
    public static SmoothCameraHelper instance = new SmoothCameraHelper();

    public float smoothCamFilterX;
    public float smoothCamFilterY;
    public float smoothCamPartialTicks;
    public float smoothCamYaw;
    public float smoothCamPitch;
    public MouseFilter mouseFilterXAxis = new MouseFilter();
    public MouseFilter mouseFilterYAxis = new MouseFilter();

    public void update(float mouseSensitivity, float multiplier)
    {
        float f = mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;
        this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, multiplier * f1);
        this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, multiplier * f1);
        this.smoothCamPartialTicks = 0.0F;
        this.smoothCamYaw = 0.0F;
        this.smoothCamPitch = 0.0F;
    }

    public float[] getAngles(float mouseSensitivity, float partialTicks, float deltaX, float deltaY, boolean invertMouse)
    {
        int b0 = invertMouse ? -1 : 1;

        float f1 = mouseSensitivity * 0.6F + 0.2F;
        float f2 = f1 * f1 * f1 * 8.0F;
        float f3 = deltaX * f2;
        float f4 = deltaY * f2;

        this.smoothCamYaw += f3;
        this.smoothCamPitch += f4;
        float f5 = partialTicks - this.smoothCamPartialTicks;
        this.smoothCamPartialTicks = partialTicks;
        float nf3 = this.smoothCamFilterX * f5;
        float nf4 = this.smoothCamFilterY * f5;

        return new float[]{nf3, nf4 * b0};
    }

    public float[] getOriginalAngles(float mouseSensitivity, float partialTicks, float deltaX, float deltaY, boolean invertMouse)
    {
        int b0 = invertMouse ? -1 : 1;

        float f1 = mouseSensitivity * 0.6F + 0.2F;
        float f2 = f1 * f1 * f1 * 8.0F;
        float f3 = deltaX * f2;
        float f4 = deltaY * f2;

        return new float[]{f3, f4 * b0};
    }
}
