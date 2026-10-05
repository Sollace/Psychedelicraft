package ivorius.psychedelicraft.client.render;

import java.util.function.Function;
import java.util.stream.IntStream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public abstract class ZeroScreen {
    private static final Identifier[] TEXTURES = IntStream.range(0, 8)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/reality_rift/zero_screen_" + i + ".png"))
            .toArray(Identifier[]::new);

    private static final Function<Identifier, RenderLayer> PS_ZERO_SCREEN = Util.memoize(texture -> RenderLayer.of("ps_zero_screen", RenderSetup.builder(PSShaders.ZERO_MATTER)
            .texture("Sampler0", texture)
            .translucent()
            .build()
    ));

    public static RenderLayer layer(float ticks) {
        int seed = MathHelper.floor(ticks * 0.5F);
        return PS_ZERO_SCREEN.apply(TEXTURES[seed % TEXTURES.length]);
    }

    @FunctionalInterface
    public interface Renderable {
        void render(RenderLayer layer, float u, float v);
    }
}
