package ivorius.psychedelicraft.client.render.command;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;

public class OrderedEffectRenderCommandQueue extends EffectRenderCommandQueue implements OrderedRenderCommandQueue {

    public static OrderedRenderCommandQueue of(OrderedRenderCommandQueue queue, Customisations customisations) {
        return queue instanceof OrderedEffectRenderCommandQueue c ? c : new OrderedEffectRenderCommandQueue(queue, customisations);
    }

    private final OrderedRenderCommandQueue delegate;
    private final Int2ObjectMap<RenderCommandQueue> stages = new Int2ObjectOpenHashMap<>();

    private OrderedEffectRenderCommandQueue(OrderedRenderCommandQueue delegate, Customisations customisations) {
        super(delegate, customisations);
        this.delegate = delegate;
    }

    @Override
    public RenderCommandQueue getBatchingQueue(int order) {
        if (order == 0) {
            return this;
        }
        return stages.computeIfAbsent(order, i -> new EffectRenderCommandQueue(delegate.getBatchingQueue(i), customisations));
    }
}
