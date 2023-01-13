/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.client.render.effect.EffectWrapper;
import ivorius.psychedelicraft.client.render.effect.ScreenEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 * Updated by Sollace on 4 Jan 2023
 */
@Deprecated
public abstract class ScreenEffectWrapper<S extends ScreenEffect> implements EffectWrapper {
    public S screenEffect;

    protected ScreenEffectWrapper(S screenEffect) {
        this.screenEffect = screenEffect;
    }

    @Override
    public void apply(float partialTicks, ScreenEffect.PingPong pingPong, Framebuffer depthBuffer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int ticks = mc.inGameHud.getTicks();

        setScreenEffectValues(partialTicks, ticks);

        if (screenEffect.shouldApply(ticks + partialTicks)) {
            screenEffect.apply(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), ticks + partialTicks, pingPong);
        }
    }

    public abstract void setScreenEffectValues(float partialTicks, int ticks);
}
