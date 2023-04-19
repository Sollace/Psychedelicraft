package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface BlockConstructionUtils {
    static AbstractBlock.Settings leaves(BlockSoundGroup soundGroup) {
        return AbstractBlock.Settings.of(Material.LEAVES)
                .strength(0.2f)
                .ticksRandomly()
                .sounds(soundGroup)
                .nonOpaque()
                .allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves)
                .suffocates(BlockConstructionUtils::never)
                .blockVision(BlockConstructionUtils::never);
    }

    static PillarBlock log(MapColor topColor, MapColor sideColor) {
        return new PillarBlock(AbstractBlock.Settings.of(Material.WOOD,
                state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topColor : sideColor)
                .strength(2).sounds(BlockSoundGroup.WOOD));
    }

    static AbstractBlock.Settings plant(BlockSoundGroup soundGroup) {
        return Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(soundGroup);
    }

    static AbstractBlock.Settings pottedPlant() {
        return Settings.of(Material.DECORATION).breakInstantly().nonOpaque();
    }

    static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }
    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static Boolean always(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return true;
    }
    static boolean always(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

}
