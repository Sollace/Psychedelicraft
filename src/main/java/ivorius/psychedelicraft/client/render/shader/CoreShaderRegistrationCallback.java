package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.resource.ResourceFactory;

public interface CoreShaderRegistrationCallback {
    Event<CoreShaderRegistrationCallback> EVENT = EventFactory.createArrayBacked(CoreShaderRegistrationCallback.class, callbacks -> {
        return (manager, shaderList) -> {
            for (var callback : callbacks) {
                callback.call(manager, shaderList);
            }
        };
    });

    void call(ResourceFactory factory, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> shaderList) throws IOException;
}
