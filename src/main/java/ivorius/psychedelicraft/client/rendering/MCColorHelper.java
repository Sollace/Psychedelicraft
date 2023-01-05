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
}
