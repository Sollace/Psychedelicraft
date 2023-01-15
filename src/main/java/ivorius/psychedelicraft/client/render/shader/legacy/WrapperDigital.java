/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigital implements EffectWrapper {
    private final WrapperDigitalMD digitalMD;
    private final WrapperDigitalPD digitalPD;

    public WrapperDigital(String utils) {
        digitalMD = new WrapperDigitalMD(utils);
        digitalPD = new WrapperDigitalPD(utils);
    }

    @Override
    public void update() {
        digitalMD.update();
        digitalPD.update();
    }

    @Override
    public void apply(float partialTicks, IvShaderInstance2D.PingPong pingPong, @Nullable Framebuffer depthBuffer) {
        if (depthBuffer != null) {
            digitalPD.apply(partialTicks, pingPong, depthBuffer);
        } else {
            digitalMD.apply(partialTicks, pingPong, depthBuffer);
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return digitalPD.wantsDepthBuffer(partialTicks) || digitalMD.wantsDepthBuffer(partialTicks);
    }
}
