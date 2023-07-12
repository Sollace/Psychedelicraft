package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;

public interface CoreShaderRegistrationCallback {
    Event<CoreShaderRegistrationCallback> EVENT = EventFactory.createArrayBacked(CoreShaderRegistrationCallback.class, callbacks -> {
        return (manager, shaderList) -> {
            for (var callback : callbacks) {
                callback.call(manager, shaderList);
            }
        };
    });

    void call(ResourceManager factory, List<Pair<Shader, Consumer<Shader>>> shaderList) throws IOException;
}
