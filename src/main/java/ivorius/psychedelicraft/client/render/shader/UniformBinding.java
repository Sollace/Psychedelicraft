package ivorius.psychedelicraft.client.render.shader;

import java.util.*;
import java.util.function.Consumer;
import com.mojang.blaze3d.buffers.Std140Builder;

import net.minecraft.util.Identifier;

public interface UniformBinding {
    void bindUniforms(float tickDelta, int screenWidth, int screenHeight, Consumer<Consumer<UniformSetter>> pass);

    public interface UniformSetter {
        void set(String name, Consumer<Std140Builder> values);
    }

    static UniformBinding.Set start() {
        return new Set();
    }

    final class Set {
        final Map<Identifier, UniformBinding> programBindings = new HashMap<>();

        public Set program(Identifier fragmentShaderId, UniformBinding binding) {
            programBindings.put(fragmentShaderId, binding);
            return this;
        }
    }
}
