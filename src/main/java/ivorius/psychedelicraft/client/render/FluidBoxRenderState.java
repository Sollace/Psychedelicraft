/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import ivorius.psychedelicraft.item.component.ItemFluids;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.google.common.base.MoreObjects;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public record FluidBoxRenderState(int light, int overlay, List<Vertex> vertices, @Nullable FluidAppearance appearance, @Nullable ItemRenderState item, @Nullable CrumblingOverlayCommand crumblingOverlay) implements OrderedRenderCommandQueue.Custom {
    public static final Direction[] ALL = Direction.values();

    @Override
    public void render(Entry transform, VertexConsumer buffer) {
        int color = appearance == null ? Colors.WHITE : appearance.color();
        Random random = Random.create();
        @Nullable Sprite sprite = appearance == null ? item == null ? null : item.getParticleSprite(random) : appearance.sprite();

        var pos = new Vector4f();
        var finalBuffer = RenderUtil.applyCrumbling(sprite == null ? buffer : sprite.getTextureSpecificVertexConsumer(buffer), crumblingOverlay);
        vertices.forEach(vertex -> {
            transform.getPositionMatrix().transform(vertex.position(), pos);
            finalBuffer.vertex(pos.x(), pos.y(), pos.z(), color, vertex.u(), vertex.v(), overlay, light, vertex.normal().getOffsetX(), vertex.normal().getOffsetY(), vertex.normal().getOffsetZ());
        });
    }

    public RenderLayer renderLayer() {
        return RenderLayers.entityTranslucent(MoreObjects.firstNonNull(appearance, FluidAppearance.EMPTY).texture());
    }

    public void render(MatrixStack matrices) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public record Vertex(Vector4fc position, float u, float v, Direction normal) {}

    public static class Builder {
        private int light = 0;
        private int overlay = 0;

        private final List<Vertex> vertices = new ArrayList<>();
        @Nullable
        private FluidAppearance appearance;
        @Nullable
        private ItemRenderState item;
        @Nullable
        private CrumblingOverlayCommand crumblingOverlay;

        private Builder() { }

        public Builder light(int light) {
            this.light = light;
            return this;
        }

        public Builder crumbling(@Nullable CrumblingOverlayCommand crumblingOverlay) {
            this.crumblingOverlay = crumblingOverlay;
            return this;
        }

        public Builder overlay(int overlay) {
            this.overlay = overlay;
            return this;
        }

        public Builder texture(FluidAppearance fluids) {
            appearance = fluids;

            return this;
        }

        public Builder texture(ItemRenderState item) {
            this.item = item;
            return this;
        }

        public Builder shape(VoxelShape shape) {
            for (Box box : shape.getBoundingBoxes()) {
                box(box, ALL);
            }
            return this;
        }

        public Builder box(Box box, Direction... directions) {
            return face((float)box.minX, (float)box.minY, (float)box.minZ, (float)box.getLengthX(), (float)box.getLengthY(), (float)box.getLengthZ(), directions);
        }

        public Builder face(float x, float y, float z, float width, float height, float length, Direction... directions) {
            return appendFaces(x, y, z, width, height, length, directions);
        }

        private Builder appendFaces(float x, float y, float z, float width, float height, float length, Direction... directions) {
            for (Direction direction : directions) {
                switch (direction) {
                    case DOWN:
                        vertex(x, y, z, 0, 0, direction);
                        vertex(x + width, y, z, 1, 0, direction);
                        vertex(x + width, y, z + length, 1, 1, direction);
                        vertex(x, y, z + length, 0, 1, direction);
                        break;
                    case UP:
                        vertex(x, y + height, z, 0, 0, direction);
                        vertex(x, y + height, z + length, 0, 1, direction);
                        vertex(x + width, y + height, z + length, 1, 1, direction);
                        vertex(x + width, y + height, z, 1, 0, direction);
                        break;
                    case EAST:
                        vertex(x + width, y, z, 0, 0, direction);
                        vertex(x + width, y + height, z, 1, 0, direction);
                        vertex(x + width, y + height, z + length, 1, 1, direction);
                        vertex(x + width, y, z + length, 0, 1, direction);
                        break;
                    case WEST:
                        vertex(x, y, z, 0, 0, direction);
                        vertex(x, y, z + length, 1, 0, direction);
                        vertex(x, y + height, z + length, 1, 1, direction);
                        vertex(x, y + height, z, 0, 1, direction);
                        break;
                    case NORTH:
                        vertex(x, y, z, 0, 0, direction);
                        vertex(x, y + height, z, 0, 1, direction);
                        vertex(x + width, y + height, z, 1, 1, direction);
                        vertex(x + width, y, z, 1, 0, direction);
                        break;
                    case SOUTH:
                        vertex(x, y, z + length, 0, 0, direction);
                        vertex(x + width, y, z + length, 1, 0, direction);
                        vertex(x + width, y + height, z + length, 1, 1, direction);
                        vertex(x, y + height, z + length, 0, 1, direction);
                        break;
                }
            }

            return this;
        }

        private Builder vertex(float x, float y, float z, float u, float v, Direction direction) {
            vertices.add(new Vertex(new Vector4f(x, y, z, 1), u, v, direction));
            return this;
        }

        public FluidBoxRenderState build() {
            return new FluidBoxRenderState(light, overlay, List.copyOf(vertices), appearance, item, crumblingOverlay);
        }
    }

    public record FluidAppearance(Identifier texture, @Nullable Sprite sprite, int color) {
        @SuppressWarnings("deprecation")
        public static final FluidAppearance EMPTY = new FluidAppearance(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, null, Colors.WHITE);

        public static FluidAppearance of(ItemFluids stack) {
            if (stack.isEmpty()) {
                return EMPTY;
            }

            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid().getPhysical().getStandingFluid());
            if (handler == null) {
                int color = stack.fluid().getColor(stack);
                Sprite sprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).particleSprite();
                return new FluidAppearance(sprite.getAtlasId(), sprite, color);
            }

            FluidState state = stack.fluid().getFluidState(stack);
            var sprite = handler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state)[0];
            return new FluidAppearance(
                    sprite.getAtlasId(),
                    sprite,
                    ColorHelper.fullAlpha(handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state))
            );
        }

        public static int getItemColor(ItemFluids stack) {
            if (!stack.fluid().isCustomFluid()) {
                FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid().getPhysical().getStandingFluid());
                if (handler != null) {
                    FluidState state = stack.fluid().getFluidState(stack);
                    return ColorHelper.fullAlpha(handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state));
                }
            }

            return ColorHelper.fullAlpha(stack.fluid().getColor(stack));
        }
    }



}
