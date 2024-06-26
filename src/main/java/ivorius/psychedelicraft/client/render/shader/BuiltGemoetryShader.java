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

public class BuiltGemoetryShader implements ShaderProgramSetupView {
    private final int program;
    private int lastUniformId;
    private int lastSamplerId;

    private final List<GlUniform> uniforms = new ArrayList<>();
    private final List<Sampler> samplers = new ArrayList<>();

    public BuiltGemoetryShader(int program) {
        this.program = program;
    }

    void registerUniform(GlUniform uniform) {

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
    public void attachReferencedShaders() { }

    public void bind() {
        for (var sampler : samplers) {
            sampler.bind(program);
        }
        for (var uniform : uniforms) {
            uniform.upload();
        }
    }

    record Sampler(int id, String name, Supplier<Integer> valueGetter) {
        void bind(int program) {
            GlUniform.uniform1(GlUniform.getUniformLocation(program, name), id);
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + id);
            RenderSystem.bindTexture(valueGetter.get());
        }
    }

    void addSampler(String name, Supplier<Integer> supplier) {
        final int id = ++lastSamplerId;
        GL30C.glBindFragDataLocation(program, id, name);
        samplers.add(new Sampler(id, name, supplier));
    }

    void addUniform(GlUniform uniform) {
        int location = ++lastUniformId;
        GL20C.glBindAttribLocation(program, location, uniform.getName());
        uniforms.add(uniform);
        uniform.setLocation(location);
    }

    public interface Holder {
        void attachUniformData(@Nullable BuiltGemoetryShader shader);
    }
}