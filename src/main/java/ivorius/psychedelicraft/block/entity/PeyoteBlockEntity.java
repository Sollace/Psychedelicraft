/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class PeyoteBlockEntity extends BlockEntity {
    public PeyoteBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.PEYOTE, pos, state);
    }
}
