package ivorius.psychedelicraft.client.render.shader;

import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface PostEffectPassSupplier {
    List<Pass> getPasses();

    interface Pass {
        String getId();

        RenderPipeline getPipeline();

        Map<String, GpuBuffer> getUniforms();

        void setDisabled(boolean disabled);
    }
}
