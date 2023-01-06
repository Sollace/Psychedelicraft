/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;

/**
 * Created by lukas on 25.10.14.
 */
@Environment(EnvType.CLIENT)
public class MCColorHelper {
    public static void setColor(int color, boolean hasAlpha) {
        RenderSystem.setShaderColor(
                MathUtils.r(color),
                MathUtils.g(color),
                MathUtils.b(color),
                hasAlpha ? MathUtils.a(color) : 1
        );
    }

    @Deprecated
    public static int mixColors(int left, int right, float progress) {
        return MathUtils.mixColors(left, right, progress);
    }

    public static void drawScreen(int screenWidth, int screenHeight) {
        BufferBuilder renderer = Tessellator.getInstance().getBuffer();
        renderer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        renderer.vertex(0, 0, 0).texture(0, 1).next();
        renderer.vertex(0, screenHeight, 0).texture(0, 0).next();
        renderer.vertex(screenWidth, screenHeight, 0).texture(1, 0).next();
        renderer.vertex(screenWidth, 0, 0).texture(1, 1).next();
        Tessellator.getInstance().draw();
    }
}
