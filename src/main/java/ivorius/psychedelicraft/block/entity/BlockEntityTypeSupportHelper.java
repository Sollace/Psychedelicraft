package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

public interface BlockEntityTypeSupportHelper {

    static BlockEntityTypeSupportHelper of(BlockEntityType<?> type) {
        return (BlockEntityTypeSupportHelper)type;
    }

    BlockEntityTypeSupportHelper addSupportedBlocks(Block... blocks);
}
