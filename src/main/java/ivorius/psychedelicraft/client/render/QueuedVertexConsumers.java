package ivorius.psychedelicraft.client.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Lets old-style immediate rendering code (which transforms its own vertices) run against the deferred render command queue.
 * Vertices are recorded per layer and replayed when the queue is drawn.
 */
public class QueuedVertexConsumers implements VertexConsumerProvider {
    private static final MatrixStack IDENTITY = new MatrixStack();

    private final Map<RenderLayer, Recorder> buffers = new LinkedHashMap<>();
    private final OrderedRenderCommandQueue queue;

    public static void submit(OrderedRenderCommandQueue queue, Consumer<VertexConsumerProvider> renderer) {
        QueuedVertexConsumers vertices = new QueuedVertexConsumers(queue);
        renderer.accept(vertices);
        vertices.submit();
    }

    private QueuedVertexConsumers(OrderedRenderCommandQueue queue) {
        this.queue = queue;
    }

    /**
     * Replacement for the removed ItemRenderer#renderItem(ItemStack, ...). Only works with vertices obtained from {@link #submit}.
     */
    public static void renderItem(ItemStack stack, ItemDisplayContext displayContext, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertices, @Nullable World world, int seed) {
        // new state each time: the queue keeps references to the state's quad lists until it is drawn
        ItemRenderState state = new ItemRenderState();
        MinecraftClient.getInstance().getItemModelManager().clearAndUpdate(state, stack, displayContext, world, null, seed);
        state.render(matrices, ((QueuedVertexConsumers)vertices).queue, light, overlay, 0);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return buffers.computeIfAbsent(layer, l -> new Recorder());
    }

    private void submit() {
        buffers.forEach((layer, recorder) -> queue.submitCustom(IDENTITY, layer, (entry, vertices) -> recorder.replay(vertices)));
        buffers.clear();
    }

    private static final class Recorder implements VertexConsumer {
        private final List<Consumer<VertexConsumer>> ops = new ArrayList<>();

        private VertexConsumer add(Consumer<VertexConsumer> op) {
            ops.add(op);
            return this;
        }

        void replay(VertexConsumer target) {
            ops.forEach(op -> op.accept(target));
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            return add(v -> v.vertex(x, y, z));
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return add(v -> v.color(red, green, blue, alpha));
        }

        @Override
        public VertexConsumer color(int argb) {
            return add(v -> v.color(argb));
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return add(c -> c.texture(u, v));
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return add(c -> c.overlay(u, v));
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return add(c -> c.light(u, v));
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return add(v -> v.normal(x, y, z));
        }

        @Override
        public VertexConsumer lineWidth(float width) {
            return add(v -> v.lineWidth(width));
        }
    }
}
