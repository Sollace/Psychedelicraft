package ivorius.psychedelicraft.fluid.container;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface FluidCauldronBehavior {
    CauldronBehavior AIR = (state, world, pos, player, hand, stack) -> {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
        ItemFluids fluid = t.fluids();
        @Nullable
        Block cauldron = fluid.fluid().getPhysical().getCauldron();

        if (cauldron != null && t.fluids().amount() >= FluidVolumes.GLASS_BOTTLE) {
            int levels = Math.min(t.fluids().amount() / FluidVolumes.GLASS_BOTTLE, LeveledCauldronBlock.MAX_LEVEL);
            Item item = stack.getItem();
            t.withdraw(levels * FluidVolumes.GLASS_BOTTLE);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            setCauldronState(world, pos, fluid, levels);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    };
    CauldronBehavior WATER = createCauldronInteraction((w, pos) -> SimpleFluid.of(Fluids.WATER).getDefaultStack());
    CauldronBehavior LAVA = createCauldronInteraction((w, pos) -> SimpleFluid.of(Fluids.LAVA).getDefaultStack());
    CauldronBehavior OTHER_FLUID = createCauldronInteraction((w, pos) -> w.getBlockEntity(pos, PSBlockEntities.CAULDRON).map(FluidCauldronBlock.Data::getFluid).orElse(ItemFluids.EMPTY));

    static void register(Item item) {
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.AIR);
        CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.LAVA);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.WATER);
        FluidCauldronBlock.BEHAVIOUR.put(item, FluidCauldronBehavior.OTHER_FLUID);
    }

    static CauldronBehavior createCauldronInteraction(BiFunction<World, BlockPos, ItemFluids> fluidTypeSupplier) {
        return (state, world, pos, player, hand, stack) -> {
            Item item = stack.getItem();
            ItemFluids fluidType = fluidTypeSupplier.apply(world, pos);
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());

            int maxTransferred = FluidCapacity.get(stack);

            if (t.fluids().isEmpty() && maxTransferred >= FluidVolumes.GLASS_BOTTLE) {
                int levelChange = maxTransferred / FluidVolumes.GLASS_BOTTLE;
                ItemFluids fluid = fluidType.ofAmount(levelChange * FluidVolumes.GLASS_BOTTLE);

                if (!t.canAccept(fluid)) {
                    return ActionResult.FAIL;
                }
                if (!world.isClient) {
                    t.deposit(fluid);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    decrementFluidLevel(state, world, pos, fluidType);
                    setCauldronState(world, pos, fluidType, getFluidLevel(state) - levelChange);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
                    world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ActionResult.SUCCESS;
            }

            if (!t.fluids().canCombine(fluidType) || t.fluids().amount() < FluidVolumes.GLASS_BOTTLE || !incrementFluidLevel(state, world, pos, fluidType)) {
                return ActionResult.PASS;
            }

            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            t.withdraw(FluidVolumes.GLASS_BOTTLE);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            return ActionResult.SUCCESS;
        };
    }

    private static int getFluidLevel(BlockState state) {
        return state.getOrEmpty(LeveledCauldronBlock.LEVEL).orElse(state.isOf(Blocks.CAULDRON) ? 0 : LeveledCauldronBlock.MAX_LEVEL);
    }

    private static boolean incrementFluidLevel(BlockState state, World world, BlockPos pos, ItemFluids fluidType) {
        int level = getFluidLevel(state);
        if (level < LeveledCauldronBlock.MAX_LEVEL) {
            setCauldronState(world, pos, fluidType, level + 1);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            return true;
        }
        return false;
    }

    private static void decrementFluidLevel(BlockState state, World world, BlockPos pos, ItemFluids fluidType) {
        setCauldronState(world, pos, fluidType, getFluidLevel(state) - 1);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
    }

    private static void setCauldronState(World world, BlockPos pos, ItemFluids fluidType, int level) {
        world.setBlockState(pos, getCauldronState(fluidType, level));
        world.getBlockEntity(pos, PSBlockEntities.CAULDRON).ifPresent(data -> data.setFluid(fluidType));
    }

    private static BlockState getCauldronState(ItemFluids fluidType, int level) {
        if (level <= 0) {
            return Blocks.CAULDRON.getDefaultState();
        }
        if (level >= LeveledCauldronBlock.MAX_LEVEL && fluidType.isOf(Fluids.LAVA)) {
            return Blocks.LAVA_CAULDRON.getDefaultState();
        }
        return fluidType.fluid().getPhysical().getCauldron().getDefaultState().withIfExists(LeveledCauldronBlock.LEVEL, level);
    }
}
