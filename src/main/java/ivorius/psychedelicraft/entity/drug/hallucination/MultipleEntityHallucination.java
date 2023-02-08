/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.List;
import java.util.stream.IntStream;

import ivorius.psychedelicraft.PSTags;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MultipleEntityHallucination extends EntityHallucination {
    private final List<Vec3d> positions;

    public MultipleEntityHallucination(PlayerEntity player) {
        super(player, PSTags.Entities.MULTIPLE_ENTITY_HALLUCINATIONS);
        positions = IntStream.range(0, random.nextBetween(5, 15)).mapToObj(i -> {
            double distance = Math.max(2, i / 10F);
            return new Vec3d(
                    random.nextTriangular(0, distance),
                    random.nextTriangular(0, distance),
                    random.nextTriangular(0, distance)
            );
        }).toList();
    }

    @Override
    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        positions.forEach(pos -> {
            float scale = entity.getWidth();
            super.renderModel(matrices, vertices,
                    x + pos.x * scale,
                    y + pos.y * scale,
                    z + pos.z * scale,
                    pitch, yaw, tickDelta);
        });
    }
}
