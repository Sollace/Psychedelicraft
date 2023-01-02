/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Created by lukas on 25.10.14.
 */
@Environment(EnvType.CLIENT)
public class MCColorHelper {
    public static void setColor(int color, boolean hasAlpha) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        if (hasAlpha) {
            float alpha = (color >> 24 & 255) / 255.0F;
            RenderSystem.setShaderColor(1.0F * red, 1.0F * green, 1.0F * blue, alpha);
        } else {
            RenderSystem.setShaderColor(1.0F * red, 1.0F * green, 1.0F * blue, 1);
        }
    }

    @Deprecated
    public static int mixColors(int left, int right, float progress) {
        return MathUtils.mixColors(left, right, progress);
    }
}
