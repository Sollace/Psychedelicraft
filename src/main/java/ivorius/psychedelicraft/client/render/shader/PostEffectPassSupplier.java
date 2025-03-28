package ivorius.psychedelicraft.client.render.shader;

import java.util.List;

import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.ShaderProgram;

public interface PostEffectPassSupplier {
    List<Pass> getPasses();

    interface Pass {
        String getId();

        void setDisabled();

        void setUniformUpdater(PostEffectPassSupplier.UniformUpdater updater);
    }

    interface UniformUpdater {
        List<PostEffectPipeline.Uniform> accept(ShaderProgram program);
    }
}
