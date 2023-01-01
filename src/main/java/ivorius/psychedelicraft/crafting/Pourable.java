/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 03.11.14.
 */
public interface Pourable {
    boolean canPour(ItemStack stack, ItemStack dst);

    boolean canReceivePour(ItemStack stack, ItemStack src);
}
