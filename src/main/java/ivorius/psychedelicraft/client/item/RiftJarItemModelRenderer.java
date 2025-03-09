package ivorius.psychedelicraft.client.item;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.client.render.blocks.RiftJarBlockEntityRenderer;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public class RiftJarItemModelRenderer implements SpecialModelRenderer<Float> {
    private final RiftJarBlockEntityRenderer renderer;

    public RiftJarItemModelRenderer(RiftJarBlockEntityRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Float getData(ItemStack stack) {
        return RiftFractionComponent.getRiftFraction(stack);
    }

    @Override
    public void render(Float data, ModelTransformationMode modelTransformationMode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {
        renderer.renderAsItem(data, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false), matrices, vertices, light, overlay);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels entityModels) {
            return new RiftJarItemModelRenderer(new RiftJarBlockEntityRenderer());
        }
    }
}
