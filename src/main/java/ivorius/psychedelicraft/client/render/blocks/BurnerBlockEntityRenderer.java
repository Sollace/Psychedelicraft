/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.block.entity.contents.SmallContents;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class BurnerBlockEntityRenderer implements BlockEntityRenderer<BurnerBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final TextRenderer textRenderer;

    public BurnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        itemRenderer = context.getItemRenderer();
        textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(BurnerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (entity.getContents() instanceof Processable.Context contents && !entity.getContainer().isEmpty()) {
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

            PlacedDrinksModelProvider.INSTANCE.renderDrink("burner", entity.getContainer(), matrices, vertices, light, overlay);

            if (contents instanceof LargeContents largeContents) {
                renderFlaskMultiFluids(largeContents, matrices, vertices, light, overlay);
                renderIngredients(entity, largeContents, matrices, vertices, light, overlay);
            }
            if (contents instanceof SmallContents smallContents) {
                renderFlaskSingleFluid(smallContents, matrices, vertices, light, overlay);
            }

            matrices.pop();
        }

        renderLabels(entity, tickDelta, matrices, vertices, light, overlay);
    }

    static boolean shouldRenderLabel(BlockEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity.getPos() != null
                && entity.getWorld() != null
                && client.getEntityRenderDispatcher().camera.getBlockPos().getSquaredDistance(entity.getPos()) < 4096
                && client.crosshairTarget instanceof BlockHitResult hit && hit.getBlockPos().equals(entity.getPos());
    }

    private void renderLabels(BurnerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (!shouldRenderLabel(entity)) {
            return;
        }

        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrices.translate(0, 0, 0.5);

        var position = matrices.peek().getPositionMatrix();
        int temperature = entity.getTemperature();

        float scale = 0.005F;
        if (temperature > 100) {
            scale += tickDelta * 0.001F;
        }
        matrices.scale(scale, -scale, scale);

        String text = temperature + "";
        int width = textRenderer.getWidth(text);

        int temperatureColorComponent = MathHelper.clamp((int)(255 * (1 - temperature/100F)), 0, 255);
        int temperatureColor = ColorHelper.Argb.getArgb(255, 255, temperatureColorComponent, temperatureColorComponent);

        textRenderer.draw(text, -width / 2F, 0, temperatureColor, true, position, vertices, TextLayerType.NORMAL, 0, light);
        matrices.scale(0.9F, 0.9F, 0.9F);
        textRenderer.draw(" o", width / 4F, -textRenderer.fontHeight / 2F, temperatureColor, true, position, vertices, TextLayerType.NORMAL, 0, light);

        int maxCapacity = FluidCapacity.get(entity.getContainer());
        if (maxCapacity > 0) {
            int percentage = (int)((entity.getTotalFluidVolume() / (float)maxCapacity) * 100);
            text = switch (percentage) {
                case 100 -> "Full";
                case 0 -> "Empty";
                default -> percentage + "%";
            };
            textRenderer.draw(text, -(textRenderer.getWidth(text) - 5) / 2F, -textRenderer.fontHeight - 2, Colors.WHITE, true, position, vertices, TextLayerType.NORMAL, 0, light);
        }

        matrices.pop();
    }

    static void renderFlaskSingleFluid(SmallContents contents, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.scale(1/16F, 1/16F, 1/16F);
        Resovoir tank = contents.getPrimaryTank();
        float fluidHeight = 2.4F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
        FluidBoxRenderer.getInstance()
            .light(light).overlay(overlay)
            .position(matrices)
            .texture(vertices, tank.getContents())
            .draw(6.7F, 0.5F, 6.7F, 2.6F, fluidHeight, 2.6F, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        matrices.pop();
    }

    static void renderFlaskMultiFluids(LargeContents contents, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.scale(1/16F, 1/16F, 1/16F);
        FluidBoxRenderer renderer = FluidBoxRenderer.getInstance().light(light).overlay(overlay).position(matrices);

        float fluidStartY = 0;
        var tanks = contents.getAuxiliaryTanks();
        for (Resovoir tank : tanks) {
            renderer.texture(vertices, tank.getContents());

            float fluidHeight = 6F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
            float fluidEndY = fluidStartY + fluidHeight;

            if (fluidStartY < 1 && fluidEndY > 0) {
                if (fluidStartY <= 0) {
                    renderer.draw(5, 1, 5, 6, 1, 6, Direction.DOWN);
                }

                float maxY = Math.min(1, fluidEndY);
                renderer.draw(5, 1, 5, 6, maxY, 6, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
                if (fluidEndY <= 1) {
                    renderer.draw(5, 1, 5, 6, maxY, 6, Direction.UP);
                }
            }

            if (fluidStartY < 2 && fluidEndY > 1) {
                if (fluidStartY <= 1) {
                    renderer.draw(4, 2, 6, 1, 1, 4, Direction.DOWN);
                    renderer.draw(11, 2, 6, 1, 1, 4, Direction.DOWN);
                    renderer.draw(6, 2, 4, 4, 1, 1, Direction.DOWN);
                    renderer.draw(6, 2, 11, 4, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 1) + 1;
                float maxY = Math.min(1, fluidEndY - 1) - minY + 2;
                renderer.draw(5, minY, 5, 1, maxY, 1, Direction.WEST, Direction.NORTH);
                renderer.draw(5, minY, 10, 1, maxY, 1, Direction.WEST, Direction.SOUTH);

                renderer.draw(10, minY, 5, 1, maxY, 1, Direction.EAST, Direction.NORTH);
                renderer.draw(10, minY, 10, 1, maxY, 1, Direction.EAST, Direction.SOUTH);

                renderer.draw(4, minY, 6, 1, maxY, 4, Direction.WEST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(11, minY, 6, 1, maxY, 4, Direction.EAST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(6, minY, 4, 4, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST);
                renderer.draw(6, minY, 11, 4, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST);

                if (fluidEndY <= 2) {
                    renderer.draw(5, 2, 5, 6, maxY, 6, Direction.UP);
                    renderer.draw(4, 2, 6, 1, maxY, 4, Direction.UP);
                    renderer.draw(11, 2, 6, 1, maxY, 4, Direction.UP);
                    renderer.draw(6, 2, 4, 4, maxY, 1, Direction.UP);
                    renderer.draw(6, 2, 11, 4, maxY, 1, Direction.UP);
                }
            }

            if (fluidStartY <= 3 && fluidEndY > 2) {
                if (fluidStartY <= 2) {
                    renderer.draw(4, 3, 5, 1, 1, 1, Direction.DOWN);
                    renderer.draw(4, 3, 10, 1, 1, 1, Direction.DOWN);

                    renderer.draw(11, 3, 5, 1, 1, 1, Direction.DOWN);
                    renderer.draw(11, 3, 10, 1, 1, 1, Direction.DOWN);

                    renderer.draw(5, 3, 4, 1, 1, 1, Direction.DOWN);
                    renderer.draw(10, 3, 4, 1, 1, 1, Direction.DOWN);

                    renderer.draw(5, 3, 11, 1, 1, 1, Direction.DOWN);
                    renderer.draw(10, 3, 11, 1, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 2) + 1;
                float maxY = Math.min(4, fluidEndY - 2) - minY + 3;
                renderer.draw(4, minY, 5, 1, maxY, 6, Direction.WEST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(11, minY, 5, 1, maxY, 6, Direction.EAST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(5, minY, 4, 6, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST);
                renderer.draw(5, minY, 11, 6, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST);

                renderer.draw(4, minY, 5, 1, maxY, 6, Direction.UP);
                renderer.draw(11, minY, 5, 1, maxY, 6, Direction.UP);
                renderer.draw(5, minY, 4, 6, maxY, 8, Direction.UP);
            }

            fluidStartY = fluidEndY;
        }

        matrices.pop();
    }

    private void renderIngredients(BurnerBlockEntity entity, LargeContents contents, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        float itemScale = 0.35F;

        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(itemScale, itemScale, itemScale);

        Random rng = Random.create(entity.getPos().asLong());

        float y = 0;

        for (var i : contents.getIngredients().getCounts().object2IntEntrySet()) {
            ItemStack stack = i.getKey().getDefaultStack();
            for (int j = 0; j < i.getIntValue(); j++) {
                matrices.push();

                matrices.translate((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + y);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((rng.nextFloat() * 360) - 180));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((rng.nextFloat() * 360) - 180));
                y -= 0.1F;

                itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertices, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        matrices.pop();
    }
}
