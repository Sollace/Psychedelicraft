/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated
public class ShaderMain {

    protected void setUniformFloats(String name, float...ints) {}
    protected void setUniformInts(String name, int...ints) {}

    protected void setUniformMatrix(String name, Matrix4f matrix) {}

    public boolean activate(float partialTicks, float ticks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        setUniformFloats("ticks", ShaderContext.ticks());
        setUniformInts("worldTime", (int)ShaderContext.time());
        setUniformFloats("playerPos", (float) mc.player.getX(), (float) mc.player.getY(), (float) mc.player.getZ());
        setUniformFloats("bigWaves", ShaderContext.hallucinations().getBigWaveStrength(partialTicks));
        setUniformFloats("smallWaves", ShaderContext.hallucinations().getSmallWaveStrength(partialTicks));
        setUniformFloats("wiggleWaves", ShaderContext.hallucinations().getWiggleWaveStrength(partialTicks));
        setUniformFloats("distantWorldDeformation", ShaderContext.hallucinations().getDistantWorldDeformationStrength(partialTicks));

        float surfaceFractalStrength = MathHelper.clamp(ShaderContext.hallucinations().getSurfaceFractalStrength(partialTicks), 0, 1);
        if (surfaceFractalStrength > 0) {
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
            setUniformFloats("fractal0TexCoords", sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
        }
        setUniformFloats("surfaceFractal", surfaceFractalStrength);
        return true;
    }
}
