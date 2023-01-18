package ivorius.psychedelicraft.client.render;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class ZeroScreen extends RenderLayer {
    private ZeroScreen() {super(null, null, null, 0, false, false, null, null);}

    public static final Identifier[] TEXTURES = IntStream.range(0, 8)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/reality_rift/zero_screen_" + i + ".png"))
            .toArray(Identifier[]::new);
    public static final float X_PIXELS = 140 / 2F;
    public static final float Y_PIXELS = 224 / 2F;

    private static final Random RNG = new Random(0L);

    private static final Function<Identifier, RenderLayer> PS_ZERO_SCREEN = Util.memoize(texture -> of("ps_zero_screen",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS, 256, false, false, MultiPhaseParameters.builder()
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .lightmap(DISABLE_LIGHTMAP)
            .program(END_GATEWAY_PROGRAM)
            .texture(new RenderPhase.Texture(texture, false, false))
            .build(false)
    ));

    public static void render(float ticks, Renderable action) {
        GLStateProxy.enableTexCoords();
        GLStateProxy.setResolution(ZeroScreen.X_PIXELS, ZeroScreen.Y_PIXELS);
        int seed = MathHelper.floor(ticks * 0.5F);
        RNG.setSeed(seed);

        action.render(
                RenderLayer.getEntityTranslucentEmissive(TEXTURES[seed % TEXTURES.length]),
                //PS_ZERO_SCREEN.apply(TEXTURES[seed % TEXTURES.length]),
                RNG.nextInt(10) * 0.1F * ZeroScreen.X_PIXELS,
                RNG.nextInt(8) * 0.125f * ZeroScreen.Y_PIXELS
        );
        GLStateProxy.clearResolution();
        GLStateProxy.disableScreenTexCoords();
    }

    @FunctionalInterface
    public interface Renderable {
        void render(RenderLayer layer, float u, float v);
    }
}
