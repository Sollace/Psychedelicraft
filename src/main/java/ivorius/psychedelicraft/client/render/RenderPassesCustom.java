/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 23.10.14.
 */
@Deprecated(forRemoval = true)
public interface RenderPassesCustom
{
    boolean hasAlphaCustom(ItemStack stack, int pass);

    int getRenderPassesCustom(ItemStack stack);
}
