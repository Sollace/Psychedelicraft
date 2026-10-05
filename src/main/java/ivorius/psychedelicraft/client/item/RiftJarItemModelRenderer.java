package ivorius.psychedelicraft.client.item;

import java.util.function.Consumer;

import org.joml.Vector3fc;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.client.render.blocks.RiftJarBlockEntityRenderer;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

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
    public void render(Float data, ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int outlineColor) {
        RiftJarBlockEntityRenderer.State state = renderer.createRenderState();
        state.fillPercentage = data;
        state.facing = Direction.SOUTH;
        state.lightmapCoordinates = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        renderer.renderJarBody(state, matrices, queue);
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
            return new RiftJarItemModelRenderer(new RiftJarBlockEntityRenderer());
        }
    }
}
