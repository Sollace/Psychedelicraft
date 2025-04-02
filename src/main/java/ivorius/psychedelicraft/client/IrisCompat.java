package ivorius.psychedelicraft.client;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.common.base.Suppliers;

import ivorius.psychedelicraft.client.render.shader.FloatSupplier;
import ivorius.psychedelicraft.client.render.shader.UniformCollection;

public final class IrisCompat {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final Supplier<Class<?>> FloatSupplier = lookupClass("net.irisshaders.iris.gl.uniform.FloatSupplier");
    private static final Supplier<Class<?>> UniformHolder = lookupClass("net.irisshaders.iris.gl.uniform.UniformHolder");
    private static final Supplier<Class<?>> UniformUpdateFrequency = lookupClass("net.irisshaders.iris.gl.uniform.UniformUpdateFrequency");

    private static Supplier<Class<?>> lookupClass(String className) {
        return Suppliers.memoize(() -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T wrapFloatSupplier(FloatSupplier value) {
        try {
            MethodHandle getter = LOOKUP.findVirtual(FloatSupplier.class, "getAsFloat", MethodType.methodType(float.class));
            CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                    "getAsFloat",
                    MethodType.methodType(FloatSupplier.get(), FloatSupplier.class),
                    MethodType.methodType(float.class),
                    getter, MethodType.methodType(float.class)
            );
            Object supplier = site.getTarget().invoke(value);
            return (T)supplier;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> void invokeAddUniform(Object reference, String methodName, String uniformName, @Nullable T value, Class<? super T> valueType) {
        if (value == null) {
            return;
        }
        try {
            LOOKUP.findVirtual(UniformHolder.get(), methodName,
                    MethodType.methodType(UniformHolder.get(), UniformUpdateFrequency.get(), String.class, valueType))
                .invoke(reference, UniformUpdateFrequency.get().getEnumConstants()[2], uniformName, value);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static UniformCollection wrapUniformHolder(Object reference) {
        return new UniformCollection() {
            @Override
            public void vec1(String name, FloatSupplier value) {
                if (!"PS_FractalFractureStrength".equals(name)) {
                    invokeAddUniform(reference, "uniform1f", name, wrapFloatSupplier(value), FloatSupplier.get());
                }
            }

            @Override
            public void vec3(String name, Supplier<Vector3f> value) {
                invokeAddUniform(reference, "uniform3f", name, value, Supplier.class);
            }

            @Override
            public void vec4(String name, Supplier<Vector4f> value) {
                if (!"PS_WavesMatrix".equals(name)) {
                    invokeAddUniform(reference, "uniform4f", name, value, Supplier.class);
                }
            }
        };
    }
}
