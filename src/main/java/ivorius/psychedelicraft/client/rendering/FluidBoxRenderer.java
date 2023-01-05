/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.Direction;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.fluids.Resovoir;
import ivorius.psychedelicraft.fluids.SimpleFluid;

import org.joml.Vector3f;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public class FluidBoxRenderer {
    private static final TextureBounds DEFAULT_BOUNDS = new TextureBounds(0, 0, 1, 1);
    private static final FluidBoxRenderer INSTANCE = new FluidBoxRenderer();

    public static FluidBoxRenderer getInstance() {
        return INSTANCE;
    }

    private static void renderFluid(float x, float y, float z, float width, float height, float length, float texX0, float texX1, float texY0, float texY1, Direction... directions) {
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);

        for (Direction direction : directions) {
            Vector3f normal = direction.getUnitVector();
            tessellator.getBuffer().normal(normal.x, normal.y, normal.z);

            switch (direction) {
                case DOWN:
                    buffer.vertex(x, y, z).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y, z).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y, z + length).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y, z + length).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    break;
                case UP:
                    buffer.vertex(x, y + height, z).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y + height, z + length).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z + length).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    break;
                case EAST:
                    buffer.vertex(x + width, y, z).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z + length).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y, z + length).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    break;
                case WEST:
                    buffer.vertex(x, y, z).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y, z + length).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y + height, z + length).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y + height, z).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    break;
                case NORTH:
                    buffer.vertex(x, y, z).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y + height, z).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y, z).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    break;
                case SOUTH:
                    buffer.vertex(x, y, z + length).texture(texX0, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y, z + length).texture(texX1, texY0).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x + width, y + height, z + length).texture(texX1, texY1).normal(normal.x, normal.y, normal.z).next();
                    buffer.vertex(x, y + height, z + length).texture(texX0, texY1).normal(normal.x, normal.y, normal.z).next();
                    break;
            }
        }

        tessellator.draw();
    }

    public float scale;

    private TextureBounds sprite = DEFAULT_BOUNDS;
    private boolean preparedTranslucency;
    private boolean disabledTextures;

    private FluidBoxRenderer() { }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void prepare(Resovoir tank) {
        SimpleFluid fluid = tank.getFluidType();

        if (fluid.isEmpty()) {
            prepare(tank.getStack());
        } else {
            sprite = DEFAULT_BOUNDS;
            // TODO: fluids probably use their own texture rather than just a color
            MCColorHelper.setColor(fluid.getColor(tank.getStack()), fluid.isTranslucent());
            disableTexture();

            if (fluid.isTranslucent()) {
                prepareTranslucency();
            }
        }
    }

    public void prepare(ItemStack stack) {
        sprite = new TextureBounds(MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack).getParticleSprite());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        prepareTranslucency();
    }

    private void disableTexture() {
        RenderSystem.disableTexture();
        disabledTextures = true;
    }

    private void prepareTranslucency() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        preparedTranslucency = true;
    }

    public void renderFluid(float x, float y, float z, float width, float height, float length, Direction... directions) {
        renderFluid(x * scale, y * scale, z * scale, width * scale, height * scale, length * scale, sprite.x0, sprite.x1, sprite.y0, sprite.y1, directions);
    }

    public void cleanUp() {
        Tessellator.getInstance().draw();

        if (preparedTranslucency) {
            RenderSystem.disableBlend();
        }
        if (disabledTextures) {
            RenderSystem.enableTexture();
        }
    }

    record TextureBounds(float x0, float x1, float y0, float y1) {
        TextureBounds(Sprite sprite) {
            this(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
    }

}
