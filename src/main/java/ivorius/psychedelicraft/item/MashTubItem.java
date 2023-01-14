/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import net.minecraft.block.Block;

public class MashTubItem extends FlaskItem {
    public MashTubItem(Block block, Settings settings) {
        super(block, settings, MashTubBlockEntity.MASH_TUB_CAPACITY);
    }
}
