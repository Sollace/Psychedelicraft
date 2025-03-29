package ivorius.psychedelicraft.client.render.shader;

import java.util.List;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gl.PostEffectPipeline;

public interface PostEffectPassSupplier {
    List<Pass> getPasses();

    interface Pass {
        String getId();

        RenderPipeline getPipeline();

        void setDisabled();

        void setUniformUpdater(PostEffectPassSupplier.UniformUpdater updater);
    }

    interface UniformUpdater {
        List<PostEffectPipeline.Uniform> accept(RenderPipeline pipeline);
    }
}
