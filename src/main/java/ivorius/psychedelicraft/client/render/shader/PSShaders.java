package ivorius.psychedelicraft.client.render.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

public interface PSShaders {
    RenderPipeline.Snippet RENDERTYPE_ZERO_MATTER_SNIPPET = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withVertexShader(Psychedelicraft.id("core/rendertype_zero_matter"))
            .withFragmentShader(Psychedelicraft.id("core/rendertype_zero_matter"))
            .withSampler("Sampler0")
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .buildSnippet();

    RenderPipeline ZERO_MATTER = RenderPipelines.register(
        RenderPipeline.builder(RENDERTYPE_ZERO_MATTER_SNIPPET)
            .withLocation(Psychedelicraft.id("pipeline/zero_matter"))
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .build()
    );

    static void bootstrap() {}
}
