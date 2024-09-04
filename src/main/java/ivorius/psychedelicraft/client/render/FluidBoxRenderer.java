/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public class FluidBoxRenderer {
    private static final Vector4f POSITION_VECTOR = new Vector4f(0, 0, 0, 1);
    private static final FluidBoxRenderer INSTANCE = new FluidBoxRenderer();

    public static FluidBoxRenderer getInstance() {
        return INSTANCE;
    }

    private float scale = 1;
    private int light = 0;
    private int overlay = 0;

    @Nullable
    private Sprite sprite;

    @Nullable
    private VertexConsumer buffer;

    private int color = Colors.WHITE;

    @Nullable
    private Matrix4f position;

    private FluidBoxRenderer() { }

    public FluidBoxRenderer scale(float scale) {
        this.scale = scale;
        return this;
    }

    public FluidBoxRenderer light(int light) {
        this.light = light;
        return this;
    }

    public FluidBoxRenderer overlay(int overlay) {
        this.overlay = overlay;
        return this;
    }

    public FluidBoxRenderer position(MatrixStack position) {
        this.position = position.peek().getPositionMatrix();
        return this;
    }

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, ItemFluids fluids) {
        if (fluids.isEmpty()) {
            sprite = null;
            buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
            color = Colors.WHITE;
        } else {
            FluidAppearance appearance = FluidAppearance.of(fluids);

            sprite = appearance.sprite();
            color = appearance.color();
            buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(appearance.texture()));
        }

        return this;
    }

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, ItemStack stack) {
        sprite = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack).getParticleSprite();
        buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
        color = Colors.WHITE;
        return this;
    }

    public void draw(float x, float y, float z, float width, float height, float length, Direction... directions) {
        renderFluidFace(x, y, z, width, height, length, directions);
    }

    private void vertex(float x, float y, float z, float u, float v, Direction direction) {
        POSITION_VECTOR.set(x, y, z, 1);
        position.transform(POSITION_VECTOR);
        buffer.vertex(
                POSITION_VECTOR.x * scale, POSITION_VECTOR.y * scale, POSITION_VECTOR.z * scale,
                ColorHelper.Argb.fullAlpha(color),
                u, v,
                overlay, light,
                direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()
        );
    }

    private void renderFluidFace(float x, float y, float z, float width, float height, float length, Direction... directions) {
        if (sprite == null) {
            return;
        }
        for (Direction direction : directions) {
            switch (direction) {
                case DOWN:
                    vertex(x, y, z, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x + width, y, z, sprite.getMaxU(), sprite.getMinV(), direction);
                    vertex(x + width, y, z + length, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x, y, z + length, sprite.getMinU(), sprite.getMaxV(), direction);
                    break;
                case UP:
                    vertex(x, y + height, z, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x, y + height, z + length, sprite.getMinU(), sprite.getMaxV(), direction);
                    vertex(x + width, y + height, z + length, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x + width, y + height, z, sprite.getMaxU(), sprite.getMinV(), direction);
                    break;
                case EAST:
                    vertex(x + width, y, z, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x + width, y + height, z, sprite.getMaxU(), sprite.getMinV(), direction);
                    vertex(x + width, y + height, z + length, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x + width, y, z + length, sprite.getMinU(), sprite.getMaxV(), direction);
                    break;
                case WEST:
                    vertex(x, y, z, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x, y, z + length, sprite.getMaxU(), sprite.getMinV(), direction);
                    vertex(x, y + height, z + length, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x, y + height, z, sprite.getMinU(), sprite.getMaxV(), direction);
                    break;
                case NORTH:
                    vertex(x, y, z, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x, y + height, z, sprite.getMinU(), sprite.getMaxV(), direction);
                    vertex(x + width, y + height, z, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x + width, y, z, sprite.getMaxU(), sprite.getMinV(), direction);
                    break;
                case SOUTH:
                    vertex(x, y, z + length, sprite.getMinU(), sprite.getMinV(), direction);
                    vertex(x + width, y, z + length, sprite.getMaxU(), sprite.getMinV(), direction);
                    vertex(x + width, y + height, z + length, sprite.getMaxU(), sprite.getMaxV(), direction);
                    vertex(x, y + height, z + length, sprite.getMinU(), sprite.getMaxV(), direction);
                    break;
            }
        }
    }

    public record FluidAppearance(Identifier texture, Sprite sprite, int color) {
        public static FluidAppearance of(ItemFluids stack) {
            /*return stack.fluid().getFlowTexture(stack).map(texture -> {
                Sprite sprite = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(texture);
                return new FluidAppearance(sprite.getAtlasId(), sprite, Colors.WHITE);
            }).orElseGet(() -> {*/
                int color = stack.fluid().getColor(stack);
                Sprite sprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();

                if (!stack.fluid().isCustomFluid()) {
                    FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid().getPhysical().getStandingFluid());
                    if (handler != null) {
                        FluidState state = stack.fluid().getPhysical().getStandingFluid().getDefaultState();
                        color = handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state);
                        sprite = handler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state)[0];
                    }
                }

                return new FluidAppearance(sprite.getAtlasId(), sprite, color);
            //});
        }

        public static int getItemColor(ItemFluids stack) {
            if (!stack.fluid().isCustomFluid()) {
                FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid().getPhysical().getStandingFluid());
                if (handler != null) {
                    FluidState state = stack.fluid().getPhysical().getStandingFluid().getDefaultState();
                    return ColorHelper.Argb.fullAlpha(handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state));
                }
            }

            return ColorHelper.Argb.fullAlpha(stack.fluid().getColor(stack));
        }

        public float[] rgba() {
            return new float[] {
                    MathUtils.r(color),
                    MathUtils.g(color),
                    MathUtils.b(color),
                    1
            };
        }
    }

}
