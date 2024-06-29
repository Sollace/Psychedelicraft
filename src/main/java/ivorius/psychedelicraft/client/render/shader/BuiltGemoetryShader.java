package ivorius.psychedelicraft.client.render.shader;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.memASCII;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

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
        private int lastAttributeId;
        private int lastFragmentId;

        public Builder(int program, int lastAttributeId, int lastFragmentId) {
            this.program = program;
            this.lastAttributeId = lastAttributeId;
            this.lastFragmentId = lastFragmentId;

            //GL30C.glBindAttribLocation(this.program, 5, "vertexColor");
            //GL30C.glBindFragDataLocation(this.program, 1, "Color");
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
                } else {
                    System.out.println("Sampler not defined: " + sampler.name);
                    System.out.println("Program log: " + GL20C.glGetProgramInfoLog(program));

                    int[] uniformCount = {0};
                    GL20C.glGetProgramiv(program, GL20C.GL_ACTIVE_UNIFORMS, uniformCount);

                    System.out.println("Declared uniforms: ");
                    for (int i = 0; i < uniformCount[0]; i++) {
                        MemoryStack stack = stackGet(); int stackPointer = stack.getPointer();
                        try {
                            IntBuffer length = stack.ints(0);
                            int maxLength = GL20C.glGetProgrami(program, GL20C.GL_ACTIVE_UNIFORM_MAX_LENGTH);
                            ByteBuffer name = stack.malloc(maxLength);
                            ByteBuffer size = stack.malloc(255);
                            ByteBuffer type = stack.malloc(255);
                            GL20C.nglGetActiveUniform(program, i, maxLength, memAddress(length), memAddress(size), memAddress(type), memAddress(name));
                            System.out.println("    " + memASCII(name, length.get(0)));
                        } finally {
                            stack.setPointer(stackPointer);
                        }
                    }
                    System.out.println("=============================================");

                    int[] count = {0};
                    int[] shaders = new int[2];
                    GL20C.glGetAttachedShaders(program, count, shaders);
                    for (int i = 0; i < count[0]; i++) {
                        //System.out.println(GL20C.glGetShaderSource(shaders[i]));
                       // System.out.println("Pause");
                        System.out.println("Shader log: " + GL20C.glGetShaderInfoLog(shaders[i]));
                    }
                }
            });
            List<GlUniform> uniforms = new ArrayList<>();
            this.uniforms.forEach(uniform -> {
                int location = GL20C.glGetUniformLocation(program, uniform.getName());
                if (location != -1) {
                    uniforms.add(uniform);
                    uniform.setLocation(location);
                } else {
                    System.out.println("Uniform not defined: " + uniform.getName());
                }
            });

            return new BuiltGemoetryShader(program, uniforms, samplers);
        }
    }
}