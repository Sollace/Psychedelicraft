package ivorius.psychedelicraft.fluid.container;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface FluidCauldronBehavior {
    CauldronBehavior AIR = (state, world, pos, player, hand, stack) -> {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
        @Nullable
        Block cauldron = t.fluids().fluid().getPhysical().getCauldron();
        if (cauldron != null && t.fluids().amount() > FluidVolumes.GLASS_BOTTLE) {
            int levels = Math.min(t.fluids().amount() / FluidVolumes.GLASS_BOTTLE, LeveledCauldronBlock.MAX_LEVEL);
            Item item = stack.getItem();
            t.withdraw(levels * FluidVolumes.GLASS_BOTTLE);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, cauldron.getDefaultState().with(LeveledCauldronBlock.LEVEL, levels));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    };
    CauldronBehavior WATER = createCauldronInteraction(SimpleFluid.forVanilla(Fluids.WATER));
    CauldronBehavior LAVA = createCauldronInteraction(SimpleFluid.forVanilla(Fluids.LAVA));

    static void register(Item item) {
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.map().put(item, FluidCauldronBehavior.AIR);
        CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.map().put(item, FluidCauldronBehavior.LAVA);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(item, FluidCauldronBehavior.WATER);
    }

    static CauldronBehavior createCauldronInteraction(SimpleFluid fluidType) {
        return (state, world, pos, player, hand, stack) -> {
            Item item = stack.getItem();
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
            if (t.fluids().isEmpty()) {
                ItemFluids fluid = fluidType.getDefaultStack(FluidVolumes.GLASS_BOTTLE);
                if (!t.canAccept(fluid)) {
                    return ItemActionResult.FAIL;
                }
                if (!world.isClient) {
                    t.deposit(fluid);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
                    world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ItemActionResult.SUCCESS;
            }

            if (t.fluids().fluid() != fluidType || t.fluids().amount() < FluidVolumes.GLASS_BOTTLE || !tryIncrementFluidLevel(state, world, pos)) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            t.withdraw(FluidVolumes.GLASS_BOTTLE);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            return ItemActionResult.SUCCESS;
        };
    }

    private static boolean tryIncrementFluidLevel(BlockState state, World world, BlockPos pos) {
        if (state.get(LeveledCauldronBlock.LEVEL) < LeveledCauldronBlock.MAX_LEVEL) {
            state = state.cycle(LeveledCauldronBlock.LEVEL);
            world.setBlockState(pos, state);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            return true;
        }
        return false;
    }
}
