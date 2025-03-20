/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.function.Function;

import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.recipe.FluidMound;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class GlassTubeBlockEntityRenderer implements BlockEntityRenderer<GlassTubeBlock.Data> {
    private static final Function<Integer, Function<Direction, VoxelShape>> SHAPE_PART_CACHE = Util.memoize(step -> {
        return GlassTubeBlock.createShapePartCache(GlassTubeBlock.RADIUS * 0.5, step / 10D, 0.1);
    });

    public GlassTubeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(GlassTubeBlock.Data entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        var contents = entity.getContents();

        BlockState state = entity.getCachedState();
        FluidBoxRenderer.getInstance().light(light).overlay(overlay).position(matrices);

        var in = state.get(GlassTubeBlock.IN).getDirection();
        if (in.isPresent()) {
            for (int i = 0; i < 5 && i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    FluidBoxRenderer.getInstance().texture(vertices, mound.get(0)).draw(SHAPE_PART_CACHE.apply(-i + 5).apply(in.get()));
                }
            }
        }

        var out = state.get(GlassTubeBlock.OUT).getDirection();
        if (out.isPresent()) {
            for (int i = 5; i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    FluidBoxRenderer.getInstance().texture(vertices, mound.get(0)).draw(SHAPE_PART_CACHE.apply(i - 5).apply(out.get()));
                }
            }
        }
    }
}
