/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

/**
 * Created by Sollace on Feb 6 2023
 */
public class FilledBucketItem extends BucketItem {

    public FilledBucketItem(Settings settings) {
        super(Fluids.EMPTY, settings.recipeRemainder(Items.BUCKET).translationKey(Items.BUCKET.getTranslationKey()));
        registerDispenserBehaviour(this);
    }

    @Override
    public Text getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Text.translatable("%s %s", fluids.fluid().getName(fluids), Items.BUCKET.getName(stack));
        }

        return Items.BUCKET.getName(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !ItemFluids.of(stack).isEmpty() && FluidCapacity.getPercentage(stack) < 1;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int)(ITEM_BAR_STEPS * FluidCapacity.getPercentage(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemFluids fluids = ItemFluids.of(stack);

        FluidState fluid = fluids.fluid().getFluidState(fluids);

        BlockHitResult hit = BucketItem.raycast(world, user, fluid.isEmpty()
                ? RaycastContext.FluidHandling.SOURCE_ONLY
                : RaycastContext.FluidHandling.NONE);

        if (hit.getType() == HitResult.Type.MISS) {
            return ActionResult.PASS;
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos3;
            BlockPos blockPos = hit.getBlockPos();
            Direction direction = hit.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(blockPos2, direction, stack)) {
                return ActionResult.FAIL;
            }

            if (fluid.isEmpty()) {
                ItemStack itemStack2;
                BlockState state = world.getBlockState(blockPos);
                if (state.getBlock() instanceof FluidDrainable drainable && !(itemStack2 = drainable.tryDrainFluid(user, world, blockPos, state)).isEmpty()) {
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    drainable.getBucketFillSound().ifPresent(sound -> user.playSound(sound, 1.0f, 1.0f));
                    world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                    ItemStack itemStack3 = ItemUsage.exchangeStack(stack, user, itemStack2);
                    if (!world.isClient) {
                        Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)user, itemStack2);
                    }
                    return ActionResult.SUCCESS.withNewHandStack(itemStack3);
                }
                return ActionResult.FAIL;
            }
            BlockState blockState = world.getBlockState(blockPos);
            blockPos3 = blockState.getBlock() instanceof FluidFillable && fluid.isOf(Fluids.WATER) ? blockPos : blockPos2;
            if (placeFluid(stack, fluids, fluid, user, world, blockPos3, hit)) {
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos3, stack);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return ActionResult.SUCCESS.withNewHandStack(BucketItem.getEmptiedStack(stack, user));
            }

            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public static void registerDispenserBehaviour(Item item) {
        DispenserBlock.registerBehavior(item, new ItemDispenserBehavior(){
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                ServerWorld world = pointer.world();
                ItemFluids fluids = ItemFluids.of(stack);
                if (placeFluid(stack, fluids, fluids.fluid().getFluidState(fluids), null, world, blockPos, null)) {
                    return Items.BUCKET.getDefaultStack();
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
    }

    @Override
    public void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean placeFluid(ItemStack stack, ItemFluids fluids, FluidState fluidState, @Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hit) {
        if (!(fluidState.getFluid() instanceof FlowableFluid)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean canPlace = state.canBucketPlace(fluidState.getFluid());

        if (!(state.isAir() || canPlace || block instanceof FluidFillable f && f.canFillWithFluid(player, world, pos, state, fluidState.getFluid()))) {
            return hit != null && placeFluid(stack, fluids, fluidState, player, world, hit.getBlockPos().offset(hit.getSide()), null);
        }

        if (world.getDimension().ultrawarm() && !fluidState.isIn(FluidTags.LAVA)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }

        if (block instanceof FluidFillable f && fluids.amount() >= FluidCapacity.get(stack) && f.tryFillWithFluid(world, pos, state, fluidState)) {
            playEmptyingSound(fluidState.getFluid(), player, world, pos);
            return true;
        }

        if (!world.isClient && canPlace && !state.isLiquid()) {
            world.breakBlock(pos, true);
        }


        BlockState placedBlockState = fluidState.getBlockState();
        boolean placeFalling = false;
        boolean placeInto = world.getBlockState(pos).isOf(placedBlockState.getBlock());
        if (fluids.amount() < FluidCapacity.get(stack) && world.isAir(pos)) {
            placedBlockState = placedBlockState.with(Properties.LEVEL_15, 8);
            placeFalling = true;
        }

        if (placeFalling || placeInto) {
            if (placeFalling && !placeInto) {
                for (var dir : List.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP)) {
                    if (world.isAir(pos.offset(dir))) {
                        world.setBlockState(pos.offset(dir), placedBlockState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                        break;
                    }
                }
            }
            if (world.setBlockState(pos, placedBlockState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD) || state.getFluidState().isStill()) {
               playEmptyingSound(fluidState.getFluid(), player, world, pos);
               return true;
            }
        }
        return false;
    }

    private static void playEmptyingSound(Fluid fluid, @Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        SoundEvent soundEvent = FluidVariantAttributes.getEmptySound(FluidVariant.of(fluid));
        world.playSound(player, pos, soundEvent, SoundCategory.BLOCKS, 1, 1);
        world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
