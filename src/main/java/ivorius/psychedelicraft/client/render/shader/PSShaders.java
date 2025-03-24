package ivorius.psychedelicraft.client.render.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;

public interface PSShaders {
    RenderPipeline.Snippet RENDERTYPE_ZERO_MATTER_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withVertexShader(Psychedelicraft.id("core/rendertype_zero_matter"))
            .withFragmentShader(Psychedelicraft.id("core/rendertype_zero_matter"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withUniform("GameTime", UniformType.FLOAT)
            .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
            .buildSnippet();

    RenderPipeline ZERO_MATTER = RenderPipelines.register(
        RenderPipeline.builder(RENDERTYPE_ZERO_MATTER_SNIPPET)
            .withLocation(Psychedelicraft.id("pipeline/zero_matter"))
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withShaderDefine("ZERO_MATTER_LAYERS", 15).build()
    );

    static void bootstrap() {}
}
