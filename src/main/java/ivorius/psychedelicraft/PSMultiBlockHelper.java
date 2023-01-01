/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.util.math.Box;

/**
 * Created by lukas on 31.10.14.
 */
public class PSMultiBlockHelper {
    public static Box intersection(Box bb, int x, int y, int z) {
        return new Box(
            Math.max(bb.minX, x), Math.max(bb.minY, y), Math.max(bb.minZ, z),
            Math.min(bb.maxX, x + 1), Math.min(bb.maxY, y + 1), Math.min(bb.maxZ, z + 1)
        );
    }

    public static Box boundsIntersection(Box bb, int x, int y, int z) {
        return new Box(
                Math.max(bb.minX - x, 0), Math.max(bb.minY - y, 0), Math.max(bb.minZ - z, 0),
                Math.min(bb.maxX - x, 1), Math.min(bb.maxY - y, 1), Math.min(bb.maxZ - z, 1)
        );
    }
}
