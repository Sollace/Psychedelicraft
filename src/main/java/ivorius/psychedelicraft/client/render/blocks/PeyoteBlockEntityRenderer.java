/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Random;
import java.util.stream.IntStream;

public class PeyoteBlockEntityRenderer implements BlockEntityRenderer<PeyoteBlockEntity> {
    private static final Identifier[] TEXTURES = IntStream.range(0, 4)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/peyote/peyote_stage" + i + ".png"))
            .toArray(Identifier[]::new);

    private final ModelPart[] models = {
            PeyoteModel.stage0().createModel(),
            PeyoteModel.stage1().createModel(),
            PeyoteModel.stage2().createModel(),
            PeyoteModel.stage3().createModel()
    };

    public PeyoteBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(PeyoteBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);
        matrices.translate(0, 1, 0);


        Random rng = RenderUtil.random(entity.getCachedState().getRenderingSeed(entity.getPos()));

        Vec3d offset = entity.getCachedState().getModelOffset(entity.getWorld(), entity.getPos());
        matrices.translate(offset.x, offset.y, offset.z);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rng.nextInt(4) * 180));

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));

        int age = entity.getCachedState().get(PeyoteBlock.AGE) % 4;
        models[age].render(matrices, vertices.getBuffer(RenderLayer.getEntityCutout(TEXTURES[age])), light, overlay, 1, 1, 1, 1);

        matrices.pop();
    }
}
