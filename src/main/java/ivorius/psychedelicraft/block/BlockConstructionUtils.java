package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

interface BlockConstructionUtils {
    static AbstractBlock.Settings leaves(BlockSoundGroup soundGroup) {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.DARK_GREEN)
                .strength(0.2f)
                .ticksRandomly()
                .sounds(soundGroup)
                .nonOpaque()
                .allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves)
                .suffocates(BlockConstructionUtils::never)
                .blockVision(BlockConstructionUtils::never)
                .burnable()
                .pistonBehavior(PistonBehavior.DESTROY)
                .solidBlock(BlockConstructionUtils::never);
    }

    static BarrelBlock barrel(MapColor mapColor) {
        return new BarrelBlock(Settings.create()
                .mapColor(mapColor)
                .instrument(Instrument.BASS)
                .sounds(BlockSoundGroup.WOOD)
                .hardness(2)
                .burnable()
                .pistonBehavior(PistonBehavior.BLOCK));
    }

    static PillarBlock log(MapColor topColor, MapColor sideColor) {
        return new PillarBlock(AbstractBlock.Settings.create()
                .mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topColor : sideColor)
                .instrument(Instrument.BASS)
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
                .burnable());
    }

    static AbstractBlock.Settings plant(BlockSoundGroup soundGroup) {
        return Settings.create()
                .ticksRandomly()
                .mapColor(MapColor.DARK_GREEN)
                .noCollision()
                .breakInstantly()
                .sounds(soundGroup)
                .burnable()
                .pistonBehavior(PistonBehavior.DESTROY);
    }

    static FlowerPotBlock pottedPlant(Block flower, FeatureFlag ... requiredFeatures) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .breakInstantly()
                .nonOpaque()
                .pistonBehavior(PistonBehavior.DESTROY);
        if (requiredFeatures.length > 0) {
            settings = settings.requires(requiredFeatures);
        }
        return new FlowerPotBlock(flower, settings);
    }

    static ButtonBlock woodenButton(BlockSetType blockSetType, FeatureFlag ... requiredFeatures) {
        AbstractBlock.Settings settings = Settings.create()
                .noCollision()
                .strength(0.5f)
                .pistonBehavior(PistonBehavior.DESTROY);
        if (requiredFeatures.length > 0) {
            settings = settings.requires(requiredFeatures);
        }
        return new ButtonBlock(settings, blockSetType, 30, true);
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
