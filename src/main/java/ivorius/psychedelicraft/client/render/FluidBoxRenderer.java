/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.Direction;
import ivorius.psychedelicraft.fluid.Resovoir;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.util.MathUtils;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public class FluidBoxRenderer {
    private static final TextureBounds DEFAULT_BOUNDS = new TextureBounds(0, 0, 1, 1);
    private static final float[] DEFAULT_COLOR = new float[] {1, 1, 1, 1};
    private static final Vector4f POSITION_VECTOR = new Vector4f(0, 0, 0, 1);
    private static final FluidBoxRenderer INSTANCE = new FluidBoxRenderer();

    public static FluidBoxRenderer getInstance() {
        return INSTANCE;
    }

    private float scale = 1;
    private int light = 0;
    private int overlay = 0;

    private TextureBounds sprite = DEFAULT_BOUNDS;

    @Nullable
    private VertexConsumer buffer;

    private float[] color = DEFAULT_COLOR;

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

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, Resovoir tank) {
        SimpleFluid fluid = tank.getFluidType();

        if (fluid.isEmpty()) {
            texture(vertices, tank.getStack());
        } else {
            sprite = DEFAULT_BOUNDS;
            // TODO: (Sollace) fluids probably use their own texture rather than just a color
            int color = fluid.getTranslucentColor(tank.getStack());
            this.color = new float[] {
                    MathUtils.r(color),
                    MathUtils.g(color),
                    MathUtils.b(color),
                    MathUtils.a(color)
            };
            buffer = vertices.getBuffer(fluid.isTranslucent() ? RenderLayer.getTranslucent() : RenderLayer.getSolid());
        }

        return this;
    }

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, ItemStack stack) {
        sprite = new TextureBounds(MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack).getParticleSprite());
        buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
        color = DEFAULT_COLOR;
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
                color[0], color[1], color[2], color[3],
                u, v,
                light, overlay,
                direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()
        );
    }

    private void renderFluidFace(float x, float y, float z,
            float width, float height, float length,
            Direction... directions) {
        for (Direction direction : directions) {
            switch (direction) {
                case DOWN:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y, z, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y, z + length, sprite.x0, sprite.y1, direction);
                    break;
                case UP:
                    vertex(x, y + height, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y + height, z + length, sprite.x0, sprite.y1, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y0, direction);
                    break;
                case EAST:
                    vertex(x + width, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y, z + length, sprite.x0, sprite.y1, direction);
                    break;
                case WEST:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y, z + length, sprite.x1, sprite.y0, direction);
                    vertex(x, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y + height, z, sprite.x0, sprite.y1, direction);
                    break;
                case NORTH:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y + height, z, sprite.x0, sprite.y1, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y, z, sprite.x1, sprite.y0, direction);
                    break;
                case SOUTH:
                    vertex(x, y, z + length, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y, z + length, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y + height, z + length, sprite.x0, sprite.y1, direction);
                    break;
            }
        }
    }

    record TextureBounds(float x0, float x1, float y0, float y1) {
        TextureBounds(Sprite sprite) {
            this(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
    }

}
