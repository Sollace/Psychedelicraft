package ivorius.psychedelicraft.client.rendering;

import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ZeroScreen {
    public static final Identifier[] TEXTURES = IntStream.range(0, 8)
            .mapToObj(i -> Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "zero_screen_" + i + ".png"))
            .toArray(Identifier[]::new);
    public static final float X_PIXELS = 140 / 2F;
    public static final float Y_PIXELS = 224 / 2F;

    private static final Random RNG = new Random(0L);

    public static void render(float ticks, Renderable action) {
        PSRenderStates.setUseScreenTexCoords(true);
        PSRenderStates.setPixelSize(1F / ZeroScreen.X_PIXELS, -1F / ZeroScreen.Y_PIXELS);
       // RenderSystem.depthMask(false);
        RenderSystem.enableCull();

        int seed = MathHelper.floor(ticks * 0.5F);
        RNG.setSeed(seed);
        action.render(
                TEXTURES[seed % TEXTURES.length],
                RNG.nextInt(10) * 0.1F * ZeroScreen.X_PIXELS,
                RNG.nextInt(8) * 0.125f * ZeroScreen.Y_PIXELS
        );

        RenderSystem.disableCull();
       // RenderSystem.depthMask(true);
        PSRenderStates.setScreenSizeDefault();
        PSRenderStates.setUseScreenTexCoords(false);
    }

    @FunctionalInterface
    public interface Renderable {
        void render(Identifier texture, float u, float v);
    }
}
