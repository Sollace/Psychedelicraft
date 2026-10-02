package ivorius.psychedelicraft.client.render.shader;

import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import com.mojang.blaze3d.pipeline.RenderPipeline;

/**
 * Since 1.21.6 vanilla only feeds uniform blocks and samplers to its programs.
 * The mod's shaders still use plain uniforms, so we upload those ourselves right after vanilla binds a program.
 */
public interface ProgramUniforms {
    Map<RenderPipeline, Map<String, float[]>> PENDING_POST_UNIFORMS = new WeakHashMap<>();

    /**
     * Implemented on vanilla's ShaderProgram via mixin.
     */
    interface Holder {
        int psychedelicraft_getUniformLocation(String name);

        @Nullable
        BuiltGemoetryShader psychedelicraft_getGeometryShader();
    }

    static void setPostUniforms(RenderPipeline pipeline, Map<String, float[]> uniforms) {
        PENDING_POST_UNIFORMS.put(pipeline, uniforms);
    }

    /**
     * Called by the command encoder after a program has been bound for a draw.
     */
    static void onProgramBound(Holder program, RenderPipeline pipeline) {
        BuiltGemoetryShader geometry = program.psychedelicraft_getGeometryShader();
        if (geometry != null) {
            geometry.bind();
        }

        Map<String, float[]> uniforms = PENDING_POST_UNIFORMS.get(pipeline);
        if (uniforms != null) {
            uniforms.forEach((name, values) -> upload(program.psychedelicraft_getUniformLocation(name), values));
        }
    }

    static void upload(int location, float... values) {
        if (location == -1) {
            return;
        }
        switch (values.length) {
            case 1 -> GL20C.glUniform1f(location, values[0]);
            case 2 -> GL20C.glUniform2f(location, values[0], values[1]);
            case 3 -> GL20C.glUniform3f(location, values[0], values[1], values[2]);
            case 4 -> GL20C.glUniform4f(location, values[0], values[1], values[2], values[3]);
            default -> throw new IllegalArgumentException("Unsupported uniform size " + values.length);
        }
    }
}
