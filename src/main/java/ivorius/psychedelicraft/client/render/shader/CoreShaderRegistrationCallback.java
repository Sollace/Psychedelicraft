package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.ArrayList;
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

    void call(ResourceFactory factory, ArrayList<Pair<ShaderProgram, Consumer<ShaderProgram>>> shaderList) throws IOException;
}
