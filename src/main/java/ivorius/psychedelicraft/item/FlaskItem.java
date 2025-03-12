/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.text.Text;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 1 Jan 2023
 */
public class FlaskItem extends BlockItem {
    public FlaskItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Text.translatable(getTranslationKey() + ".filled", fluids.fluid().getName(fluids));
        }

        return super.getName(stack);
    }
}
