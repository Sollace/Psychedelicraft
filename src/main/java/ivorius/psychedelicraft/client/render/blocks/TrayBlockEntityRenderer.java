/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class TrayBlockEntityRenderer extends LabelledBlockEntityRenderer<TrayBlockEntity, TrayBlockEntityRenderer.State> {
    private static final Identifier FLUID_TEXTURE = Psychedelicraft.id("textures/entity/tray/fluid.png");

    private final TrayContentsModel contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().createModel());

    public TrayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(TrayBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.level = entity.getLevel();
        state.hardened = entity.isHardened();
        int percentage = (int)(100 * state.level / 50F);
        state.fillText = state.level <= 0 ? null : switch (percentage) {
            case 100 -> Text.literal("Full");
            case 0 -> Text.literal("Empty");
            default -> Text.literal(percentage + "%");
        };
        state.resultText = entity.getCraftingResult().map(ItemStack::getName).orElse(null);
        state.axis = entity.getCachedState().get(Properties.HORIZONTAL_AXIS);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.level > 0 || state.hardened) {
            matrices.push();
            matrices.translate(0.5, 1 / 16D, 0.5);
            queue.submitModel(contentsModel, state, matrices, state.hardened ? RenderLayers.entitySolid(FLUID_TEXTURE) : RenderLayers.entityTranslucent(FLUID_TEXTURE), state.lightmapCoordinates, 0, 0, null, 0, state.crumblingOverlay);
            matrices.pop();
        }
        super.render(state, matrices, queue, cameraState);
    }

    @Override
    protected void renderLabels(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        if (state.fillText != null) {
            queue.submitText(matrices, -(textRenderer.getWidth(state.fillText) - 5) / 2F, -textRenderer.fontHeight - 2, state.resultText.asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, Colors.BLUE, 0, 0);
        }

        if (state.resultText != null) {
            queue.submitText(matrices, -(textRenderer.getWidth(state.resultText) - 5) / 2F, 0, state.resultText.asOrderedText(), true, TextLayerType.NORMAL, state.lightmapCoordinates, Colors.BLUE, 0, 0);
        }
    }

    public static class State extends LabelledBlockEntityRenderer.State {
        @Nullable
        public Text fillText;
        @Nullable
        public Text resultText;
        public int level;
        public boolean hardened;
        public Axis axis = Axis.X;
    }
}
