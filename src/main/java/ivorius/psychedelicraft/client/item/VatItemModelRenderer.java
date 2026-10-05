package ivorius.psychedelicraft.client.item;

import java.util.function.Consumer;

import org.joml.Vector3fc;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.client.render.blocks.MashTubBlockEntityRenderer;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
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
    public void render(ItemFluids data, ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int outline) {
        renderer.renderAsItem(matrices, queue, light, overlay, outline);
        renderer.renderItemFill(data, matrices, queue, light, overlay);
    }

    @Override
    public void collectVertices(Consumer<Vector3fc> consumer) {
        renderer.collectVertices(consumer);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new VatItemModelRenderer(new MashTubBlockEntityRenderer());
        }
    }
}
