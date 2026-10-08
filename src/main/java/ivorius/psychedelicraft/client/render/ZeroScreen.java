package ivorius.psychedelicraft.client.render;

import java.util.function.Function;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.PSRenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface ZeroScreen {
    Identifier TEXTURE = Psychedelicraft.id("textures/effect/zero_screen.png");
    RenderLayer ZERO_SCREEN_SOLID = RenderLayer.of("zero_screen_solid", RenderSetup.builder(PSRenderPipelines.ZERO_MATTER_DEPTH)
            .texture("BitsSampler", TEXTURE)
            .translucent()
            .build()
    );
    RenderLayer ZERO_SCREEN = RenderLayer.of("zero_screen", RenderSetup.builder(PSRenderPipelines.ZERO_MATTER)
        .texture("BitsSampler", TEXTURE)
        .translucent()
        .build()
    );
    Function<Identifier, RenderLayer> TRANSLUCENT_CUTOUT = Util.memoize(texture -> RenderLayer.of("zero_screen_cutout", RenderSetup.builder(PSRenderPipelines.ZERO_MATTER_CUTOUT)
        .texture("BitsSampler", TEXTURE)
        .texture("CutoutSampler", texture)
        .translucent()
        .build()
    ));

    static RenderLayer translucentCutout(Identifier cutoutTexture) {
        return TRANSLUCENT_CUTOUT.apply(cutoutTexture);
    }
}
