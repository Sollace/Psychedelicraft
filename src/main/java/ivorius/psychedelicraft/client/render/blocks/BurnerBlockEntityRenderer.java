/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.block.entity.contents.SmallContents;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState.FluidAppearance;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class BurnerBlockEntityRenderer extends LabelledBlockEntityRenderer<BurnerBlockEntity, BurnerBlockEntityRenderer.State> {

    private final ItemModelManager modelManager;

    public BurnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        modelManager = context.itemModelManager();
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BurnerBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        int temperature = entity.getTemperature();
        int temperatureColorComponent = MathHelper.clamp((int)(255 * (1 - temperature/100F)), 0, 255);
        state.temperature = temperature;
        state.temperatureColor = ColorHelper.getArgb(255, 255, temperatureColorComponent, temperatureColorComponent);

        float ticks = MinecraftClient.getInstance().player.age + tickDelta;
        float amplitude = 5;//1.65F;

        state.xRotation = temperature >= 99 ? MathHelper.sin(ticks) * amplitude : 0;
        state.zRotation = temperature >= 99 ? MathHelper.sin(ticks + 9 + MathHelper.sin(ticks)) * amplitude : 0;
        if (entity.getContents() instanceof Processable.Context && !entity.getContainer().isEmpty()) {
            PlacedDrinksModelProvider.INSTANCE.updateRenderState("burner", ItemFluids.getItemForFluids(entity.getContainer(), ItemFluids.EMPTY), entity.getWorld(), state.item);
        } else {
            state.item.clear();
        }

        int maxCapacity = FluidCapacity.get(entity.getContainer());
        state.fillText = maxCapacity > 0 ? getFillPercentage(entity, maxCapacity) : null;

        if (entity.getContents() instanceof LargeContents contents) {
            extractFlaskMultiFluids(state, contents);

            long seed = state.blockState.getRenderingSeed(state.pos);
            Random rng = Random.create(seed);

            float y = 0;

            for (var i = 0; i < contents.getIngredients().size(); i++) {
                ItemStack stack = contents.getIngredients().getStack(i);
                for (int j = 0; j < stack.getCount(); j++) {
                    var itemState = new ItemRenderState();

                    modelManager.clearAndUpdate(itemState, stack, ItemDisplayContext.FIXED, entity.getWorld(), null, (int)seed);
                    state.ingredients.add(new State.IngredientRenderState(
                            itemState,
                            new Vec3d((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + (y -= 0.1F)),
                            (rng.nextFloat() * 360) - 180,
                            (rng.nextFloat() * 360) - 180
                    ));
                }
            }
        }
        if (entity.getContents() instanceof SmallContents smallContents) {
            extractFlaskSingleFluid(state, smallContents);
        }
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (!state.item.isEmpty()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.xRotation), 0.5F, 0, 0.5F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(state.zRotation), 0.5F, 0, 0.5F);
            matrices.translate(0, 0.12, 0);
            state.item.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);

            matrices.push();
            matrices.scale(1/16F, 1/16F, 1/16F);
            state.fluidBoxes.forEach(box -> queue.submitCustom(matrices, RenderLayers.entityTranslucent(box.appearance().texture()), box));
            matrices.pop();
            if (!state.ingredients.isEmpty()) {
                float itemScale = 0.35F;

                matrices.push();
                matrices.translate(0.5, 0, 0.5);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.scale(itemScale, itemScale, itemScale);
                state.ingredients.forEach(ingredient -> {
                    matrices.push();

                    matrices.translate(ingredient.offset());
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ingredient.xRotation()));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ingredient.zRotation()));
                    ingredient.item().render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
                    matrices.pop();
                });
                matrices.pop();
            }

            matrices.pop();
        }

        super.render(state, matrices, queue, cameraState);
    }

    @Override
    protected float getLabelScale(BurnerBlockEntity entity, float tickDelta) {
        if (entity.getTemperature() > 100) {
            return super.getLabelScale(entity, tickDelta) + tickDelta * 0.001F;
        }
        return super.getLabelScale(entity, tickDelta);
    }

    @Override
    protected void renderLabels(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        String text = String.valueOf(state.temperature);
        int width = textRenderer.getWidth(text);

        queue.submitText(matrices, -width / 2F, 0, Text.literal(text).asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, state.temperatureColor, 0, 0);
        matrices.scale(0.9F, 0.9F, 0.9F);
        queue.submitText(matrices, width / 4F, -textRenderer.fontHeight / 2F, Text.literal(" o").asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, state.temperatureColor, 0, 0);

        if (state.fillText != null) {
            queue.submitText(matrices, -(textRenderer.getWidth(state.fillText) - 5) / 2F, -textRenderer.fontHeight - 2, state.fillText.asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, Colors.WHITE, 0, 0);
        }
    }

    static void extractFlaskSingleFluid(State state, SmallContents contents) {
        Resovoir tank = contents.getPrimaryTank();
        ItemFluids fluids = tank.getContents();
        if (!fluids.isEmpty()) {
            float fluidHeight = 2.4F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
            state.fluidBoxes.add(FluidBoxRenderState.builder().light(state.lightmapCoordinates)
                .texture(FluidAppearance.of(tank.getContents()))
                .face(6.7F, 0.5F, 6.7F, 2.6F, fluidHeight, 2.6F, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                .build());
        }
    }

    static void extractFlaskMultiFluids(State state, LargeContents contents) {
        float fluidStartY = 0;
        var tanks = contents.getAuxiliaryTanks();
        for (Resovoir tank : tanks) {
            if (tank.getContents().isEmpty()) {
                continue;
            }
            var renderer = FluidBoxRenderState.builder().light(state.lightmapCoordinates).texture(FluidAppearance.of(tank.getContents()));

            float fluidHeight = 6F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
            float fluidEndY = fluidStartY + fluidHeight;

            if (fluidStartY < 1 && fluidEndY > 0) {
                if (fluidStartY <= 0) {
                    renderer.face(5, 1, 5, 6, 1, 6, Direction.DOWN);
                }

                float maxY = Math.min(1, fluidEndY);
                renderer.face(5, 1, 5, 6, maxY, 6, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
                if (fluidEndY <= 1) {
                    renderer.face(5, 1, 5, 6, maxY, 6, Direction.UP);
                }
            }

            if (fluidStartY < 2 && fluidEndY > 1) {
                if (fluidStartY <= 1) {
                    renderer.face(4, 2, 6, 1, 1, 4, Direction.DOWN)
                            .face(11, 2, 6, 1, 1, 4, Direction.DOWN)
                            .face(6, 2, 4, 4, 1, 1, Direction.DOWN)
                            .face(6, 2, 11, 4, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 1) + 1;
                float maxY = Math.min(1, fluidEndY - 1) - minY + 2;
                renderer.face(5, minY, 5, 1, maxY, 1, Direction.WEST, Direction.NORTH)
                        .face(5, minY, 10, 1, maxY, 1, Direction.WEST, Direction.SOUTH)

                        .face(10, minY, 5, 1, maxY, 1, Direction.EAST, Direction.NORTH)
                        .face(10, minY, 10, 1, maxY, 1, Direction.EAST, Direction.SOUTH)

                        .face(4, minY, 6, 1, maxY, 4, Direction.WEST, Direction.NORTH, Direction.SOUTH)
                        .face(11, minY, 6, 1, maxY, 4, Direction.EAST, Direction.NORTH, Direction.SOUTH)
                        .face(6, minY, 4, 4, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST)
                        .face(6, minY, 11, 4, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST);

                if (fluidEndY <= 2) {
                    renderer.face(5, 2, 5, 6, maxY, 6, Direction.UP)
                            .face(4, 2, 6, 1, maxY, 4, Direction.UP)
                            .face(11, 2, 6, 1, maxY, 4, Direction.UP)
                            .face(6, 2, 4, 4, maxY, 1, Direction.UP)
                            .face(6, 2, 11, 4, maxY, 1, Direction.UP);
                }
            }

            if (fluidStartY <= 3 && fluidEndY > 2) {
                if (fluidStartY <= 2) {
                    renderer.face(4, 3, 5, 1, 1, 1, Direction.DOWN)
                            .face(4, 3, 10, 1, 1, 1, Direction.DOWN)

                            .face(11, 3, 5, 1, 1, 1, Direction.DOWN)
                            .face(11, 3, 10, 1, 1, 1, Direction.DOWN)

                            .face(5, 3, 4, 1, 1, 1, Direction.DOWN)
                            .face(10, 3, 4, 1, 1, 1, Direction.DOWN)

                            .face(5, 3, 11, 1, 1, 1, Direction.DOWN)
                            .face(10, 3, 11, 1, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 2) + 1;
                float maxY = Math.min(4, fluidEndY - 2) - minY + 3;
                renderer.face(4, minY, 5, 1, maxY, 6, Direction.WEST, Direction.NORTH, Direction.SOUTH)
                        .face(11, minY, 5, 1, maxY, 6, Direction.EAST, Direction.NORTH, Direction.SOUTH)
                        .face(5, minY, 4, 6, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST)
                        .face(5, minY, 11, 6, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST)

                        .face(4, minY, 5, 1, maxY, 6, Direction.UP)
                        .face(11, minY, 5, 1, maxY, 6, Direction.UP)
                        .face(5, minY, 4, 6, maxY, 8, Direction.UP);
            }

            fluidStartY = fluidEndY;

            state.fluidBoxes.add(renderer.build());
        }
    }

    public static class State extends LabelledBlockEntityRenderer.State {
        public int temperature;
        public int temperatureColor;
        @Nullable
        public Text fillText;

        public float xRotation;
        public float zRotation;
        public final PlacedDrinksModelProvider.PlacedDrinkRenderState item = new PlacedDrinksModelProvider.PlacedDrinkRenderState();
        public List<IngredientRenderState> ingredients = List.of();
        public List<FluidBoxRenderState> fluidBoxes = List.of();

        public record IngredientRenderState(ItemRenderState item, Vec3d offset, float xRotation, float zRotation) {}
    }
}
