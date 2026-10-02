package ivorius.psychedelicraft.client.render;

import java.util.LinkedHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

/**
 * Renders an entity through its vanilla renderer but sends the resulting vertices to a caller-supplied VertexConsumerProvider,
 * the way EntityRenderDispatcher#render(entity, ..., vertices, light) used to before rendering became deferred.
 */
public final class EntityVertexRedirect {
    private static final OrderedRenderCommandQueueImpl QUEUE = new OrderedRenderCommandQueueImpl();
    private static final Forwarder FORWARDER = new Forwarder();
    @Nullable
    private static RenderDispatcher dispatcher;

    public static void render(Entity entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices) {
        MinecraftClient client = MinecraftClient.getInstance();
        var manager = client.getEntityRenderDispatcher();
        EntityRenderState state = manager.getAndUpdateRenderState(entity, tickDelta);
        state.shadowPieces.clear();
        manager.render(state, client.gameRenderer.getEntityRenderStates().cameraRenderState, x, y, z, matrices, QUEUE);

        if (dispatcher == null) {
            var buffers = client.getBufferBuilders();
            dispatcher = new RenderDispatcher(QUEUE, client.getBlockRenderManager(), FORWARDER, client.getAtlasManager(),
                    buffers.getOutlineVertexConsumers(), buffers.getEffectVertexConsumers(), client.textRenderer);
        }
        FORWARDER.parent = vertices;
        try {
            dispatcher.render();
        } finally {
            FORWARDER.parent = null;
        }
    }

    private static final class Forwarder extends VertexConsumerProvider.Immediate {
        @Nullable
        private VertexConsumerProvider parent;

        Forwarder() {
            super(new BufferAllocator(256), new LinkedHashMap<>());
        }

        @Override
        public VertexConsumer getBuffer(RenderLayer layer) {
            return parent.getBuffer(layer);
        }

        // the parent provider is drawn by whoever owns it
        @Override
        public void drawCurrentLayer() { }

        @Override
        public void draw() { }

        @Override
        public void draw(RenderLayer layer) { }
    }
}
