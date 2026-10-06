package ivorius.psychedelicraft.client.render.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;

import net.minecraft.client.gl.GlSampler;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.texture.GlTexture;

public class BuiltGemoetryShader {
    // GlStateManager only tracks units 0-11 and vanilla programs only use the first few
    private static final int FIRST_SAMPLER_UNIT = 11;

    private final List<Uniform> uniforms;
    private final List<Sampler> samplers;

    private BuiltGemoetryShader(List<Uniform> uniforms, List<Sampler> samplers) {
        this.uniforms = uniforms;
        this.samplers = samplers;
    }

    public void bind() {
        for (var sampler : samplers) {
            sampler.bind();
        }
        for (var uniform : uniforms) {
            uniform.upload.accept(uniform.location);
        }
    }

    private record Uniform(int location, IntConsumer upload) {}

    private record Sampler(int location, int unit, Supplier<GpuTexture> texture) {
        void bind() {
            GL20C.glUniform1i(location, unit);
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + unit);
            GlStateManager._bindTexture(((GlTexture)texture.get()).getGlId());
            GL33C.glBindSampler(unit, ((GlSampler)RenderLayers.BLOCK_SAMPLER.get()).getSamplerId());
        }
    }

    public interface Holder {
        void attachUniformData(@Nullable BuiltGemoetryShader shader);

        @Nullable BuiltGemoetryShader getUniformData();
    }

    public static class Builder implements UniformCollection {
        private final List<Map.Entry<String, IntConsumer>> uniforms = new ArrayList<>();
        private final List<String> samplerNames = new ArrayList<>();
        private final List<Supplier<GpuTexture>> samplerTextures = new ArrayList<>();

        public void addSampler(String sampler, Supplier<GpuTexture> supplier) {
            samplerNames.add(sampler);
            samplerTextures.add(supplier);
        }

        @Override
        public void vec1(String name, FloatSupplier value) {
            uniforms.add(Map.entry(name, location -> {
                GL20C.glUniform1f(location, value.getAsFloat());
            }));
        }

        @Override
        public void vec3(String name, Supplier<Vector3f> value) {
            uniforms.add(Map.entry(name, location -> {
                Vector3f v = value.get();
                GL20C.glUniform3f(location, v.x, v.y, v.z);
            }));
        }

        @Override
        public void vec4(String name, Supplier<Vector4f> value) {
            uniforms.add(Map.entry(name, location -> {
                Vector4f v = value.get();
                GL20C.glUniform4f(location, v.x, v.y, v.z, v.w);
            }));
        }

        /**
         * @return the bound shader, or null if the program uses none of the geometry uniforms
         */
        @Nullable
        public BuiltGemoetryShader build(int program) {
            List<Sampler> samplers = new ArrayList<>();
            for (int i = 0; i < samplerNames.size(); i++) {
                int location = GL20C.glGetUniformLocation(program, samplerNames.get(i));
                if (location != -1) {
                    samplers.add(new Sampler(location, FIRST_SAMPLER_UNIT - samplers.size(), samplerTextures.get(i)));
                }
            }
            List<Uniform> uniforms = new ArrayList<>();
            for (var uniform : this.uniforms) {
                int location = GL20C.glGetUniformLocation(program, uniform.getKey());
                if (location != -1) {
                    uniforms.add(new Uniform(location, uniform.getValue()));
                }
            }

            return samplers.isEmpty() && uniforms.isEmpty() ? null : new BuiltGemoetryShader(uniforms, samplers);
        }
    }
}
