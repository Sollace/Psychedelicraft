/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.client.render.RenderUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.client.render.BlockBreakingProgressAccessor;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState.FluidAppearance;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.BlockRenderLayers;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class MashTubBlockEntityRenderer extends LabelledBlockEntityRenderer<MashTubBlockEntity, MashTubBlockEntityRenderer.State> {
    private final ItemModelManager modelManager;

    @Nullable
    private BlockStateModel model;
    private List<Vector3fc> vertices;

    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context.textRenderer());
        modelManager = context.itemModelManager();
    }

    public MashTubBlockEntityRenderer() {
        super(MinecraftClient.getInstance().textRenderer);
        modelManager = MinecraftClient.getInstance().getItemModelManager();
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(MashTubBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumbling) {
        crumbling = getCrumblingOverlay(entity.getWorld(), entity.getPos(), cameraPos);
        super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);

        int seed = (int)state.blockState.getRenderingSeed(state.pos);

        Resovoir tank = entity.getPrimaryTank();

        state.fluidsBoxes = new ArrayList<>();
        state.fluidLevel = Stream.of(tank.getContents(), entity.getAuxiliaryFluids()).filter(stack -> !stack.isEmpty()).reduce(0.3F, (fluidHeight, stack) -> {
            float fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight += fillPercentage * 0.6F;

            state.fluidsBoxes.add(FluidBoxRenderState.builder().light(state.lightmapCoordinates)
                .texture(FluidAppearance.of(stack))
                .face(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP)
                .build());
            return fluidHeight;
        }, Float::sum);

        if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            var solids = new ItemRenderState();
            state.fluidsBoxes.add(FluidBoxRenderState.builder().light(state.lightmapCoordinates)
                    .texture(solids)
                    .face(-0.3F, 0, -0.3F, 1.6F, 0.2F, 1.6F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                    .face(-0.2F, 0, -0.2F, 1.4F, 0.3F, 1.4F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                    .build());
            modelManager.clearAndUpdate(solids, entity.solidContents, ItemDisplayContext.FIXED, entity.getWorld(), null, seed);
        }

        Random random = Random.create(seed);

        final float fullTicks = ShaderContext.ticks();

        state.ingredients = new ArrayList<>();
        entity.getSuppliedIngredients().stream()
            .flatMap(stack -> IntStream.range(0, stack.getCount()).mapToObj(i -> stack))
            .reduce(0, (c, stack) -> {
                float positionX = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float positionZ = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float rotation = random.nextFloat() * 360.0f;

                int singleDifference = c * 5;
                float bob = MathHelper.sin((fullTicks + singleDifference) / 8F) * 0.2F;
                float zSpin = -50 * MathHelper.cos((fullTicks + singleDifference) / 8F) * 0.12F;
                float ySpin = (fullTicks + c) % 360;

                var itemState = new ItemRenderState();

                modelManager.clearAndUpdate(itemState, stack, ItemDisplayContext.FIXED, entity.getWorld(), null, seed + c);

                state.ingredients.add(new State.Ingredient(itemState, new Vec3d(positionX, 0, positionZ), rotation, bob, zSpin, ySpin));

                return c + 1;
            }, Integer::sum);

        if (MinecraftClient.getInstance().debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES) && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            if (entity.getWorld() != null && entity.getPos() != null && entity.getCachedState().getBlock() instanceof FluidFilled tub) {
                state.outline = new Box(
                        0, 0, 0,
                        1, tub.getFluidHeight(entity.getWorld(), entity.getCachedState(), entity.getPos()), 1
                ).expand(0.001);
                state.fluidOutline = tub.getFluidCollisionBox(entity.getWorld(), entity.getCachedState(), entity.getPos());
            }
        }

        ProcessType processType = entity.getActiveProcess();
        Text process = entity.getActiveProcess().getStatus();
        if (processType != ProcessType.IDLE) {
            int progress = (int)(entity.getProgress(tickDelta) * 100);
            process = process.copy().append("... " + progress + "%");
        }
        state.statusLabel = process;
        state.percentageLabel = getFillPercentage(entity, FluidVolumes.VAT);
    }

    public void renderAsItem(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, int outline) {
        BlockState state = PSBlocks.MASH_TUB.getDefaultState();
        BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
        queue.submitBlockStateModel(matrices, BlockRenderLayers.getMovingBlockLayer(state), model, 1, 1, 1, light, overlay, outline);
        queue.submitBlockStateModel(matrices, BlockRenderLayers.getEntityBlockLayer(state), model, 1, 1, 1, light, overlay, outline);
    }

    public void renderItemFill(ItemFluids fluids, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        if (!fluids.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)fluids.amount() / FluidVolumes.VAT, 0, 2);

            float fluidHeight = 0.1F;
            fluidHeight = 0.3F + fillPercentage * 0.6F;

            FluidAppearance fluidAppearance = FluidAppearance.of(fluids);
            queue.submitCustom(matrices, RenderLayers.entityTranslucent(fluidAppearance.texture()), FluidBoxRenderState.builder()
                .light(light).overlay(overlay)
                .texture(fluidAppearance)
                .face(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP)
                .build());
        }
    }

    public void collectVertices(Consumer<Vector3fc> consumer) {
        BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(PSBlocks.MASH_TUB.getDefaultState());
        if (model != this.model) {
            this.model = model;
            vertices = model.getParts(RenderUtil.random(0)).stream()
                    .flatMap(part -> Direction.stream()
                            .flatMap(direction -> part
                                    .getQuads(direction)
                                    .stream()
                                    .flatMap(quad -> IntStream.range(0, 4).mapToObj(quad::getPosition))))
                    .distinct().toList();
        }

        vertices.forEach(consumer);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        state.fluidsBoxes.forEach(fluidBox -> {
            queue.submitCustom(matrices, fluidBox.renderLayer(), fluidBox);
        });

        matrices.push();
        matrices.translate(0, 0.75F + (state.fluidLevel / 16F - 0.02F), 0);

        state.ingredients.forEach(ingredient -> {
            matrices.push();
            matrices.translate(ingredient.offset());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ingredient.rotation()));
            matrices.scale(0.3F, 0.3F, 0.3F);
            matrices.translate(0, ingredient.bob(), -0.2F);
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(ingredient.zSpin()));
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(ingredient.ySpin()));
            ingredient.item().render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        });

        matrices.pop();

        super.render(state, matrices, queue, cameraState);

        if (state.outline != null) {
            RenderUtil.submitOutline(matrices, queue, VoxelShapes.cuboid(state.outline), RenderUtil.OUTLINE_COLOR);
        }

        if (state.fluidOutline != null) {
            matrices.push();
            matrices.translate(-state.fluidOutline.minX - ((state.fluidOutline.getLengthX() - 1) / 2), -state.fluidOutline.minY, -state.fluidOutline.minZ - ((state.fluidOutline.getLengthZ() - 1) / 2));
            RenderUtil.submitOutline(matrices, queue, VoxelShapes.cuboid(state.fluidOutline), Colors.WHITE);
            matrices.pop();
        }
    }

    @Override
    protected double getLabelDistanceFromCenter(MashTubBlockEntity entity) {
        return 1.8;
    }


    @Override
    protected void renderLabels(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        if (state.statusLabel != null) {
            queue.submitText(matrices, -(textRenderer.getWidth(state.statusLabel) - 5) / 2F, 0, state.statusLabel.asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, Colors.WHITE, 0, 0);
        }
        if (state.percentageLabel != null) {
            matrices.push();
            matrices.scale(0.9F, 0.9F, 0.9F);
            queue.submitText(matrices, -(textRenderer.getWidth(state.percentageLabel) - 5) / 2F, -textRenderer.fontHeight - 2, state.percentageLabel.asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, Colors.WHITE, 0, 0);
            matrices.pop();
        }
    }

    static @Nullable CrumblingOverlayCommand getCrumblingOverlay(@Nullable World world, @Nullable BlockPos center, Vec3d cameraPos) {
        if (world == null || center == null || BlockBreakingProgressAccessor.getStage(center) != 0) {
            return null;
        }
        int stage = 0;
        for (BlockPos pos : BlockPos.iterateInSquare(center, 1, Direction.EAST, Direction.SOUTH)) {
            if (world.getBlockState(pos).isOf(PSBlocks.MASH_TUB_EDGE)) {
                stage = Math.max(stage, BlockBreakingProgressAccessor.getStage(pos));
            }
        }

        if (stage <= 0) {
            return null;
        }

        MatrixStack matrices = new MatrixStack();
        matrices.translate(new Vec3d(center).subtract(cameraPos));
        return new ModelCommandRenderer.CrumblingOverlayCommand(stage, matrices.peek());
    }

    public static final class State extends LabelledBlockEntityRenderer.State {
        @Nullable
        public Text statusLabel;
        @Nullable
        public Text percentageLabel;
        public float fluidLevel;
        public List<FluidBoxRenderState> fluidsBoxes = List.of();
        public List<Ingredient> ingredients = List.of();
        @Nullable
        public Box outline;
        @Nullable
        public Box fluidOutline;

        public record Ingredient(ItemRenderState item, Vec3d offset, float rotation, float bob, float zSpin, float ySpin) {}
    }
}
