/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class BurnerBlockEntityRenderer implements BlockEntityRenderer<BurnerBlockEntity> {
    public static final Identifier BEAKER_MODEL = Psychedelicraft.id("block/beaker");
    public static final Identifier BEAKER_TEXTURE = Psychedelicraft.id("textures/block/beaker.png");
    private static final Random RNG = Random.create();
    private static final long SEED = 42L;

    public BurnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(BurnerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (entity.hasBottle()) {
            BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(BEAKER_MODEL);
            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
            matrices.push();

            if (entity.getTemperature() >= 99) {
                float ticks = MinecraftClient.getInstance().player.age + tickDelta;
                float amplitude = 5;//1.65F;
                matrices.translate(0.5, 0, 0.5);

                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(ticks) * amplitude));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(ticks + 9 + MathHelper.sin(ticks)) * amplitude));
                matrices.translate(-0.5, 0, -0.5);
            }
            matrices.translate(0, 0.12, 0);
            renderBakedItemModel(model, matrices, buffer, light, overlay, Colors.WHITE);
            matrices.pop();
        }
    }

    private void renderBakedItemModel(BakedModel model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        for (Direction direction : Direction.values()) {
            RNG.setSeed(SEED);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, RNG), light, overlay, color);
        }
        RNG.setSeed(SEED);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, RNG), light, overlay, color);
    }

    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, int color) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            vertices.quad(entry, bakedQuad, MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), MathUtils.a(color), light, overlay);
        }
    }

}
