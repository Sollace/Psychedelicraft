package ivorius.psychedelicraft.client.render.shader;

import java.util.function.Supplier;

import org.joml.Vector3f;
import org.joml.Vector4f;

public interface UniformCollection {
    void vec1(String name, FloatSupplier value);

    void vec3(String name, Supplier<Vector3f> value);

    void vec4(String name, Supplier<Vector4f> value);
}
