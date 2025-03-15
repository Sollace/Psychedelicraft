/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.Random;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class MashTubBlockEntityRenderer extends LabelledBlockEntityRenderer<MashTubBlockEntity> {
    private static final MashTubBlockEntity ITEM_ENTITY = PSBlockEntities.MASH_TUB.instantiate(BlockPos.ORIGIN, PSBlocks.MASH_TUB.getDefaultState());

    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    public static void renderStack(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ITEM_ENTITY.getPrimaryTank().setContents(ItemFluids.of(stack));
        MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
                ITEM_ENTITY.getCachedState(),
                ITEM_ENTITY.getPos(),
                MinecraftClient.getInstance().world,
                matrices,
                vertices.getBuffer(RenderLayer.getCutout()), false,
                MinecraftClient.getInstance().world.random);
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(ITEM_ENTITY, matrices, vertices, light, overlay);
    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();

        float fluidHeight = 0.1F;

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!stack.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight = 0.3F + fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        } else if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            FluidBoxRenderer.getInstance()
                .texture(vertices, entity.solidContents)
                .draw(-0.5F, 0, -0.5F, 2, 0.5F, 2, Direction.UP);
        }

        matrices.push();
        matrices.translate(0, 0.75f, 0);

        Object2IntMap<Item> ingredients = entity.getSuppliedIngredients().getCounts();

        long seed = entity.getPos().asLong() + 1;
        Random random = new Random(seed);

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
                WorldRenderer.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box, 0, 1, 0, 0.2F);

                box = tub.getFluidCollisionBox(entity.getWorld(), entity.getCachedState(), entity.getPos());

                matrices.translate(-box.minX - ((box.getLengthX() - 1) / 2), -box.minY, -box.minZ - ((box.getLengthZ() - 1) / 2));
                WorldRenderer.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box, 1, 1, 1, 1);
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
}
