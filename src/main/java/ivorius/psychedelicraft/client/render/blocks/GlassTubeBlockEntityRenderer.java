/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState.FluidAppearance;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState;
import ivorius.psychedelicraft.recipe.FluidMound;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class GlassTubeBlockEntityRenderer implements BlockEntityRenderer<GlassTubeBlock.Data, GlassTubeBlockEntityRenderer.State> {
    private static final Function<Integer, Function<Direction, VoxelShape>> SHAPE_PART_CACHE = Util.memoize(step -> {
        return GlassTubeBlock.createShapePartCache(GlassTubeBlock.RADIUS * 0.5, step / 10D, 0.1);
    });

    public GlassTubeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(GlassTubeBlock.Data entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        var contents = entity.getContents();

        BlockState blockState = entity.getCachedState();

        state.droplets = new ArrayList<>();

        blockState.get(GlassTubeBlock.IN).getDirection().ifPresent(in -> {
            for (int i = 0; i < 5 && i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    var shape = SHAPE_PART_CACHE.apply(5 - i).apply(in);
                    var fluid = FluidAppearance.of(mound.get(0));
                    state.droplets.add(new State.Droplet(shape, fluid));
                }
            }
        });

        blockState.get(GlassTubeBlock.OUT).getDirection().ifPresent(out -> {
            for (int i = 5; i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    var shape = SHAPE_PART_CACHE.apply(i - 5).apply(out);
                    var fluid = FluidAppearance.of(mound.get(0));
                    state.droplets.add(new State.Droplet(shape, fluid));
                }
            }
        });
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        state.droplets.forEach(droplet -> {
            queue.submitCustom(matrices, RenderLayers.armorTranslucent(droplet.fluid().texture()), FluidBoxRenderState.builder()
                    .light(state.lightmapCoordinates)
                    .crumbling(state.crumblingOverlay)
                    .texture(droplet.fluid())
                    .shape(droplet.shape())
                    .build());
        });
    }

    public static class State extends BlockEntityRenderState {
        public List<Droplet> droplets = List.of();

        public record Droplet(VoxelShape shape, FluidAppearance fluid) {}
    }
}
