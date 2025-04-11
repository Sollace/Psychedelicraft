package ivorius.psychedelicraft.client.render;

import java.util.SortedSet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;

public interface BlockBreakingProgressAccessor {
    Long2ObjectMap<SortedSet<BlockBreakingInfo>> getBlockBreakingProgressions();

    static int getStage(BlockPos pos) {
        if (MinecraftClient.getInstance().worldRenderer == null) {
            return -1;
        }
        SortedSet<BlockBreakingInfo> info = ((BlockBreakingProgressAccessor)MinecraftClient.getInstance().worldRenderer).getBlockBreakingProgressions().get(pos.asLong());
        return info == null || info.isEmpty() ? 0 : info.last().getStage();
    }
}
