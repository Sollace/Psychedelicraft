/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import net.minecraft.item.*;

/**
 * Created by lukas on 18.10.14.
 */
public class SpecialFoodItem extends Item {
    public int eatSpeed;

    public SpecialFoodItem(Settings settings, int eatSpeed) {
        super(settings);
        this.eatSpeed = eatSpeed;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return eatSpeed;
    }
}
