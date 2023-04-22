package ivorius.psychedelicraft.block;

import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;

public interface ShapeUtil {

    static VoxelShape createCenteredShape(double x, double y, double z) {
        return Block.createCuboidShape(8 - x, 0, 8 - z, 8 + x, y, 8 + z);
    }
}
