package ivorius.psychedelicraft.client.render.shader;

import java.util.*;
import java.util.function.Supplier;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.util.Identifier;

public interface UniformBinding {
    UniformBinding EMPTY = (uniforms, tickDelta, screenWidth, screenHeight, pass) -> pass.run();

    void bindUniforms(UniformSetter uniforms, float tickDelta, int screenWidth, int screenHeight, Runnable pass);

    public interface UniformSetter {
        void set(String name, Supplier<List<Float>> valueGetter);

        default void set(String name, float value) {
            set(name, Suppliers.ofInstance(List.of(value)));
        }

        default void set(String name, float... values) {
            set(name, Suppliers.ofInstance(FloatList.of(values)));
        }

        default void set(String name, Vector3fc values) {
            set(name, values.x(), values.y(), values.z());
        }

        default void set(String name, Vector4fc values) {
            set(name, values.x(), values.y(), values.z(), values.w());
        }

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

        public Set program(Identifier programName, UniformBinding binding) {
            programBindings.put(programName.withPrefixedPath("post/").toString(), binding);
            return this;
        }
    }
}
