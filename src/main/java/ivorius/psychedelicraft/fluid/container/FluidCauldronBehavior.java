package ivorius.psychedelicraft.fluid.container;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
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
        @Nullable
        Block cauldron = t.fluids().fluid().getPhysical().getCauldron();

        // TODO: Lava cauldrons do not have levels
        int amountRequired = cauldron.getDefaultState().contains(LeveledCauldronBlock.LEVEL) ? FluidVolumes.GLASS_BOTTLE : FluidVolumes.BUCKET;

        if (cauldron != null && t.fluids().amount() >= amountRequired) {
            int levels = Math.min(t.fluids().amount() / amountRequired, LeveledCauldronBlock.MAX_LEVEL);
            Item item = stack.getItem();
            t.withdraw(levels * FluidVolumes.GLASS_BOTTLE);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, cauldron.getDefaultState().withIfExists(LeveledCauldronBlock.LEVEL, levels));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    };
    CauldronBehavior WATER = createCauldronInteraction(SimpleFluid.of(Fluids.WATER));
    CauldronBehavior LAVA = createCauldronInteraction(SimpleFluid.of(Fluids.LAVA));

    static void register(Item item) {
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.AIR);
        CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.LAVA);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(item, FluidCauldronBehavior.WATER);
    }

    static CauldronBehavior createCauldronInteraction(SimpleFluid fluidType) {
        return (state, world, pos, player, hand, stack) -> {
            Item item = stack.getItem();
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());

            // TODO: Lava cauldrons do not have levels
            int minimumFluidMoved = state.contains(LeveledCauldronBlock.LEVEL) ? FluidVolumes.GLASS_BOTTLE : FluidVolumes.BUCKET;

            if (t.fluids().isEmpty()) {
                ItemFluids fluid = fluidType.getDefaultStack(minimumFluidMoved);

                if (!t.canAccept(fluid)) {
                    return ActionResult.FAIL;
                }
                if (!world.isClient) {
                    t.deposit(fluid);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    if (state.contains(LeveledCauldronBlock.LEVEL)) {
                        LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                    } else {
                        state = Blocks.CAULDRON.getDefaultState();
                        world.setBlockState(pos, state);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
                    world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ActionResult.SUCCESS;
            }

            if (t.fluids().fluid() != fluidType || t.fluids().amount() < minimumFluidMoved || !tryIncrementFluidLevel(state, world, pos)) {
                return ActionResult.PASS;
            }

            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            t.withdraw(minimumFluidMoved);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            return ActionResult.SUCCESS;
        };
    }

    private static boolean tryIncrementFluidLevel(BlockState state, World world, BlockPos pos) {
        if (state.contains(LeveledCauldronBlock.LEVEL) && state.get(LeveledCauldronBlock.LEVEL) < LeveledCauldronBlock.MAX_LEVEL) {
            state = state.cycle(LeveledCauldronBlock.LEVEL);
            world.setBlockState(pos, state);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            return true;
        }
        return false;
    }
}
