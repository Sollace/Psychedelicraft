package ivorius.psychedelicraft.client.render;

import java.util.function.Function;
import java.util.stream.IntStream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
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

    protected static final ShaderProgram ZERO_MATTER_PROGRAM = new ShaderProgram(PSShaders::getRenderTypeZeroMatterProgram);

    private static final Function<Identifier, RenderLayer> PS_ZERO_SCREEN = Util.memoize(texture -> of("ps_zero_screen",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS, 256, false, false, MultiPhaseParameters.builder()
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .lightmap(DISABLE_LIGHTMAP)
            .program(ZERO_MATTER_PROGRAM)
            .cull(DISABLE_CULLING)
            .texture(new Texture(texture, false, false))
            .build(false)
    ));

    public static void render(float ticks, Renderable action) {
        int seed = MathHelper.floor(ticks * 0.5F);
        var rng = RenderUtil.random(seed);
        action.render(
                PS_ZERO_SCREEN.apply(TEXTURES[seed % TEXTURES.length]),
                rng.nextInt(10) * 0.1F * ZeroScreen.X_PIXELS,
                rng.nextInt(8) * 0.125f * ZeroScreen.Y_PIXELS
        );
    }

    @FunctionalInterface
    public interface Renderable {
        void render(RenderLayer layer, float u, float v);
    }
}
