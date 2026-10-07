package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.PSRenderPipelines;
import net.minecraft.client.render.*;

public interface ZeroScreen {
    RenderLayer ZERO_SCREEN_SOLID = RenderLayer.of("zero_screen_solid", RenderSetup.builder(PSRenderPipelines.ZERO_MATTER_DEPTH)
            .texture("Sampler0", Psychedelicraft.id("textures/effect/zero_screen.png"))
            .translucent()
            .build()
    );
    RenderLayer ZERO_SCREEN = RenderLayer.of("zero_screen", RenderSetup.builder(PSRenderPipelines.ZERO_MATTER)
        .texture("Sampler0", Psychedelicraft.id("textures/effect/zero_screen.png"))
        .translucent()
        .build()
    );
}
