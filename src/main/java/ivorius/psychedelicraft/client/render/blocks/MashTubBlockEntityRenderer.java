/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.client.render.BlockBreakingProgressAccessor;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBaker;
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
public class MashTubBlockEntityRenderer extends LabelledBlockEntityRenderer<MashTubBlockEntity> {
    private static final MashTubBlockEntity ITEM_ENTITY = PSBlockEntities.MASH_TUB.instantiate(BlockPos.ORIGIN, PSBlocks.MASH_TUB.getDefaultState());

    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context.getTextRenderer());
    }

    public MashTubBlockEntityRenderer() {
        super(MinecraftClient.getInstance().textRenderer);
    }

    public void renderAsItem(ItemFluids fluids, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ITEM_ENTITY.getPrimaryTank().setContents(fluids);
        BlockState state = ITEM_ENTITY.getCachedState();
        BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
        MinecraftClient.getInstance().getBlockRenderManager()
            .getModelRenderer().render(matrices.peek(), vertices.getBuffer(RenderLayers.getBlockLayer(state)), state, model, 1, 1, 1, light, overlay);
        MinecraftClient.getInstance().getBlockRenderManager()
            .getModelRenderer().render(matrices.peek(), vertices.getBuffer(RenderLayers.getEntityBlockLayer(state)), state, model, 1, 1, 1, light, overlay);

        if (!fluids.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)fluids.amount() / FluidVolumes.VAT, 0, 2);

            float fluidHeight = 0.1F;
            fluidHeight = 0.3F + fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .scale(1).light(light).overlay(overlay)
                .position(matrices)
                .texture(vertices, fluids)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        }
    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {

        int damageStage = getDamageStage(entity.getWorld(), entity.getPos());
        if (damageStage != 0) {
            BlockState state = entity.getCachedState();
            BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
            VertexConsumer consumer = new OverlayVertexConsumer(vertices.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(damageStage)), matrices.peek(), 1F);
            MinecraftClient.getInstance().getBlockRenderManager()
                .getModelRenderer().render(matrices.peek(), consumer, state, model, 1, 1, 1, light, overlay);
        }

        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();

        float fluidHeight = 0.3F;

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!stack.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight += fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        }

        stack = entity.getAuxiliaryFluids();

        if (!stack.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight += fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        }

        if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            FluidBoxRenderer.getInstance()
                .texture(vertices, entity.solidContents)
                .draw(-0.3F, 0, -0.3F, 1.6F, 0.2F, 1.6F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                .draw(-0.2F, 0, -0.2F, 1.4F, 0.3F, 1.4F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        }

        matrices.push();
        matrices.translate(0, 0.75f, 0);

        Object2IntMap<Item> ingredients = entity.getSuppliedIngredients().getCounts();

        long seed = entity.getPos().asLong() + 1;
        Random random = Random.create();
        random.setSeed(seed);

        for (Item item : ingredients.keySet()) {
            for (int c = 0; c < ingredients.getInt(item); c++) {
                float positionX = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float positionZ = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float rotation = random.nextFloat() * 360.0f;

                matrices.push();
                matrices.translate(positionX, fluidHeight / 16F - 0.02F, positionZ);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                matrices.scale(0.3F, 0.3F, 0.3F);

                int singleDifference = c * 5;
                float bob = MathHelper.sin((ShaderContext.ticks() + singleDifference) / 8F) * 0.2F;
                float spin = MathHelper.cos((ShaderContext.ticks() + singleDifference) / 8F) * 0.12F;

                matrices.translate(0, bob, -0.2F);
                matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-50 * spin));
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((ShaderContext.ticks() + c) % 360));
                MinecraftClient.getInstance().getItemRenderer().renderItem(item.getDefaultStack(), ModelTransformationMode.FIXED, light, overlay, matrices, vertices, entity.getWorld(), (int)seed);

                matrices.pop();
            }
        }

        matrices.pop();

        super.render(entity, tickDelta, matrices, vertices, light, overlay);

        if (MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            if (entity.getWorld() != null && entity.getPos() != null && entity.getCachedState().getBlock() instanceof FluidFilled tub) {
                Box box = new Box(
                        0, 0, 0,
                        1, tub.getFluidHeight(entity.getWorld(), entity.getCachedState(), entity.getPos()), 1
                ).expand(0.001);

                matrices.push();
                VertexRendering.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box, 0, 1, 0, 0.2F);

                box = tub.getFluidCollisionBox(entity.getWorld(), entity.getCachedState(), entity.getPos());

                matrices.translate(-box.minX - ((box.getLengthX() - 1) / 2), -box.minY, -box.minZ - ((box.getLengthZ() - 1) / 2));
                VertexRendering.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box, 1, 1, 1, 1);
                matrices.pop();
            }
        }
    }

    @Override
    protected double getLabelDistanceFromCenter(MashTubBlockEntity entity) {
        return 1.8;
    }

    @Override
    protected void renderLabels(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ProcessType processType = entity.getActiveProcess();
        Text process = entity.getActiveProcess().getStatus();
        if (processType != ProcessType.IDLE) {
            int progress = (int)(entity.getProgress(tickDelta) * 100);
            process = process.copy().append("... " + progress + "%");
        }
        textRenderer.draw(process, -(textRenderer.getWidth(process) - 5) / 2F, 0, Colors.WHITE, true, matrices.peek().getPositionMatrix(), vertices, TextLayerType.NORMAL, 0, light);
        matrices.scale(0.9F, 0.9F, 0.9F);
        Text fill = getFillPercentage(entity, FluidVolumes.VAT);
        textRenderer.draw(fill, -(textRenderer.getWidth(fill) - 5) / 2F, -textRenderer.fontHeight - 2, Colors.WHITE, true, matrices.peek().getPositionMatrix(), vertices, TextLayerType.NORMAL, 0, light);
    }

    static int getDamageStage(@Nullable World world, @Nullable BlockPos center) {
        if (world == null || center == null || BlockBreakingProgressAccessor.getStage(center) != 0) {
            return 0;
        }
        int stage = 0;
        for (BlockPos pos : BlockPos.iterateInSquare(center, 1, Direction.EAST, Direction.SOUTH)) {
            if (world.getBlockState(pos).isOf(PSBlocks.MASH_TUB_EDGE)) {
                stage = Math.max(stage, BlockBreakingProgressAccessor.getStage(pos));
            }
        }

        return stage;
    }
}
