package ivorius.psychedelicraft.client.item;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.client.render.blocks.MashTubBlockEntityRenderer;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public class VatItemModelRenderer implements SpecialModelRenderer<ItemFluids> {
    private final MashTubBlockEntityRenderer renderer;

    public VatItemModelRenderer(MashTubBlockEntityRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public ItemFluids getData(ItemStack stack) {
        return ItemFluids.of(stack);
    }

    @Override
    public void render(ItemFluids data, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {
        renderer.renderAsItem(data, matrices, vertices, light, overlay);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels entityModels) {
            return new VatItemModelRenderer(new MashTubBlockEntityRenderer());
        }
    }
}
