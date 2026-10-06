package ivorius.psychedelicraft.client.render.shader;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import com.mojang.blaze3d.pipeline.RenderPipeline;

/**
 * Since 1.21.6 vanilla only feeds uniform blocks and samplers to its programs.
 * The mod's shaders still use plain uniforms, so we upload those ourselves right after vanilla binds a program.
 */
@Deprecated
public interface ProgramUniforms {
    /**
     * Implemented on vanilla's ShaderProgram via mixin.
     */
    @Deprecated
    interface Holder {
        @Nullable
        BuiltGemoetryShader psychedelicraft_getGeometryShader();
    }

    /**
     * Called by the command encoder after a program has been bound for a draw.
     */
    @Deprecated
    static void onProgramBound(Holder program, RenderPipeline pipeline) {
        BuiltGemoetryShader geometry = program.psychedelicraft_getGeometryShader();
        if (geometry != null) {
            geometry.bind();
        }
    }

    @Deprecated
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
