/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import net.minecraft.item.*;

/**
 * Created by lukas on 18.10.14.
 */
public class ItemFoodSpecial extends Item {
    public int eatSpeed;

    public ItemFoodSpecial(Settings settings, int eatSpeed)
    {
        super(settings);
        this.eatSpeed = eatSpeed;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return eatSpeed;
    }
}
