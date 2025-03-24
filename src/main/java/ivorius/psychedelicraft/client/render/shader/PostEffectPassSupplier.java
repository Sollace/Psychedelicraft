package ivorius.psychedelicraft.client.render.shader;

import java.util.List;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gl.PostEffectPass;

public interface PostEffectPassSupplier {
    List<PostEffectPass> getPasses();

    void setPasses(List<PostEffectPass> passes);

    interface Pass {
        String getId();

        RenderPipeline getPipeline();
    }
}
