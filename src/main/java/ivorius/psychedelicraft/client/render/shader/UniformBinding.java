package ivorius.psychedelicraft.client.render.shader;

import java.util.*;

import net.minecraft.util.math.Vec3f;

public interface UniformBinding {
    UniformBinding EMPTY = (uniforms, tickDelta, screenWidth, screenHeight, pass) -> pass.run();

    void bindUniforms(UniformSetter uniforms, float tickDelta, int screenWidth, int screenHeight, Runnable pass);

    public interface UniformSetter {
        void set(String name, float value);

        void set(String name, float...values);

        void set(String name, Vec3f values);

        default boolean setIfNonZero(String name, float value) {
            set(name, value);
            return value > 0;
        }
    }

    static UniformBinding.Set start() {
        return new Set();
    }

    final class Set {
        UniformBinding global = EMPTY;

        final Map<String, UniformBinding> programBindings = new HashMap<>();

        public Set bind(UniformBinding all) {
            this.global = all;
            return this;
        }

        public Set program(String programName, UniformBinding binding) {
            programBindings.put(programName, binding);
            return this;
        }
    }
}
