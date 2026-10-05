package ivorius.psychedelicraft.client.render.command;

import java.util.List;
import java.util.function.UnaryOperator;

import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue.Custom;
import net.minecraft.client.render.command.OrderedRenderCommandQueue.LayeredCustom;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState.LeashData;
import net.minecraft.client.render.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

public class EffectRenderCommandQueue implements RenderCommandQueue {

    private final RenderCommandQueue delegate;
    protected final Customisations customisations;

    protected EffectRenderCommandQueue(RenderCommandQueue delegate, Customisations customisations) {
        this.delegate = delegate;
        this.customisations = customisations;
    }

    @Override
    public void submitBlock(MatrixStack matrices, BlockState state, int light, int overlay, int outlineColor) {

    }

    @Override
    public void submitBlockStateModel(MatrixStack matrices, RenderLayer renderLayer, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor) {
        int color = customisations.getColor(ColorHelper.fromFloats(1F, r, g, b));
        renderLayer = customisations.getRenderLayer(renderLayer);
        if (renderLayer != null) {
            delegate.submitBlockStateModel(matrices, renderLayer, model, ColorHelper.getRedFloat(color), ColorHelper.getGreenFloat(color), ColorHelper.getBlueFloat(color), light, overlay, outlineColor);
        }
    }

    @Override
    public void submitCustom(LayeredCustom customRenderer) {

    }

    @Override
    public void submitCustom(MatrixStack matrices, RenderLayer renderLayer, Custom customRenderer) {

    }

    @Override
    public void submitShadowPieces(MatrixStack matrices, float shadowRadius, List<ShadowPiece> shadowPieces) {

    }

    @Override
    public void submitLabel(MatrixStack matrices, @Nullable Vec3d nameLabelPos, int y, Text label, boolean notSneaking, int light, double squaredDistanceToCamera, CameraRenderState cameraState) {
    }

    @Override
    public void submitText(MatrixStack matrices, float x, float y, OrderedText text, boolean dropShadow, TextLayerType layerType, int light, int color, int backgroundColor, int outlineColor) {
        color = customisations.getColor(color);
        delegate.submitText(matrices, x, y, text, dropShadow, TextLayerType.SEE_THROUGH, light, color, backgroundColor, outlineColor);
    }

    @Override
    public void submitFire(MatrixStack matrices, EntityRenderState renderState, Quaternionf rotation) {

    }

    @Override
    public void submitLeash(MatrixStack matrices, LeashData leashData) {

    }

    @Override
    public <S> void submitModel(Model<? super S> model, S state, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int tintedColor, @Nullable Sprite sprite, int outlineColor, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        tintedColor = customisations.getColor(tintedColor);
        renderLayer = customisations.getRenderLayer(renderLayer);
        if (renderLayer != null) {
            delegate.submitModel(model, state, matrices, renderLayer, light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, @Nullable Sprite sprite, boolean sheeted, boolean hasGlint, int tintedColor, @Nullable CrumblingOverlayCommand crumblingOverlay, int i) {
        tintedColor = customisations.getColor(tintedColor);
        renderLayer = customisations.getRenderLayer(renderLayer);
        if (renderLayer != null) {
            delegate.submitModelPart(part, matrices, renderLayer, light, overlay, sprite, sheeted, hasGlint, tintedColor, crumblingOverlay, i);
        }
    }

    @Override
    public void submitMovingBlock(MatrixStack matrices, MovingBlockRenderState state) {

    }

    @Override
    public void submitItem(MatrixStack matrices, ItemDisplayContext displayContext, int light, int overlay, int outlineColors, int[] tintLayers, List<BakedQuad> quads, RenderLayer renderLayer, Glint glintType) {

    }

    public record Customisations(@Nullable Integer color, @Nullable UnaryOperator<@Nullable RenderLayer> layer) {
        public int getColor(int color) {
            return color() == null ? color : color();
        }

        @Nullable
        public RenderLayer getRenderLayer(RenderLayer layer) {
            return layer() == null ? layer : layer().apply(layer);
        }
    }
}
