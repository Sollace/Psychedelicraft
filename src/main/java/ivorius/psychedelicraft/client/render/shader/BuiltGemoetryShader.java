package ivorius.psychedelicraft.client.render.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgramSetupView;
import net.minecraft.client.gl.ShaderStage;

public class BuiltGemoetryShader {
    private final int program;

    private final List<GlUniform> uniforms;
    private final List<Sampler> samplers;

    public BuiltGemoetryShader(int program, List<GlUniform> uniforms, List<Sampler> samplers) {
        this.program = program;
        this.uniforms = uniforms;
        this.samplers = samplers;
    }

    public void bind() {
        for (var sampler : samplers) {
            sampler.bind(program);
        }
        for (var uniform : uniforms) {
            uniform.upload();
        }
    }

    private static class Sampler {
        private final int id;
        public int location;
        private final String name;
        private final Supplier<Integer> valueGetter;

        public Sampler(int id, String name, Supplier<Integer> valueGetter) {
            this.id = id;
            this.name = name;
            this.valueGetter = valueGetter;
        }

        void bind(int program) {
            int texId = valueGetter.get();
            GL30C.glUniform1i(location, id);
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + id);
            RenderSystem.bindTexture(texId);
        }
    }

    public interface Holder {
        void attachUniformData(@Nullable BuiltGemoetryShader shader);
    }

    public static class Builder implements ShaderProgramSetupView {
        private final List<GlUniform> uniforms = new ArrayList<>();
        private final List<Sampler> samplers = new ArrayList<>();

        private final int program;
        private int lastFragmentId;

        public Builder(int program, int lastAttributeId, int lastFragmentId) {
            this.program = program;
            this.lastFragmentId = lastFragmentId;
        }

        @Override
        public int getGlRef() {
            return program;
        }

        @Override
        public void markUniformsDirty() {
        }

        @Override
        public ShaderStage getVertexShader() {
            return null;
        }

        @Override
        public ShaderStage getFragmentShader() {
            return null;
        }

        @Override
        public void attachReferencedShaders() {}

        void addSampler(String name, Supplier<Integer> supplier) {
            samplers.add(new Sampler(++lastFragmentId, name, supplier));
        }

        void addUniform(GlUniform uniform) {
            uniforms.add(uniform);
        }

        public BuiltGemoetryShader build() {
            List<Sampler> samplers = new ArrayList<>();
            this.samplers.forEach(sampler -> {
                int location = GL20C.glGetUniformLocation(program, sampler.name);
                if (location != -1) {
                    sampler.location = location;
                    samplers.add(sampler);
                }
            });
            List<GlUniform> uniforms = new ArrayList<>();
            this.uniforms.forEach(uniform -> {
                int location = GL20C.glGetUniformLocation(program, uniform.getName());
                if (location != -1) {
                    uniforms.add(uniform);
                    uniform.setLocation(location);
                }
            });

            return new BuiltGemoetryShader(program, uniforms, samplers);
        }
    }
}