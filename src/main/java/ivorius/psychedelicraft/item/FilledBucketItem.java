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
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
public class FilledBucketItem extends Item {

    public FilledBucketItem(Settings settings) {
        super(settings.recipeRemainder(Items.BUCKET));
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
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        FluidCapacity.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemFluids fluids = ItemFluids.of(stack);
        FluidState fluid = fluids.fluid().getFluidState(fluids);

        BlockHitResult hit = BucketItem.raycast(world, user, fluid.isEmpty()
                ? RaycastContext.FluidHandling.SOURCE_ONLY
                : RaycastContext.FluidHandling.NONE);

        if (hit.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos3;
            BlockPos blockPos = hit.getBlockPos();
            Direction direction = hit.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(blockPos2, direction, stack)) {
                return TypedActionResult.fail(stack);
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
                    return TypedActionResult.success(itemStack3, world.isClient());
                }
                return TypedActionResult.fail(stack);
            }
            BlockState blockState = world.getBlockState(blockPos);
            blockPos3 = blockState.getBlock() instanceof FluidFillable && fluid.isOf(Fluids.WATER) ? blockPos : blockPos2;
            if (placeFluid(fluid, user, world, blockPos3, hit)) {
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos3, stack);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(BucketItem.getEmptiedStack(stack, user), world.isClient());
            }

            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.pass(stack);
    }

    public static void registerDispenserBehaviour(Item item) {
        DispenserBlock.registerBehavior(item, new ItemDispenserBehavior(){
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                ServerWorld world = pointer.world();
                ItemFluids fluids = ItemFluids.of(stack);
                if (placeFluid(fluids.fluid().getFluidState(fluids), null, world, blockPos, null)) {
                    return Items.BUCKET.getDefaultStack();
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static boolean placeFluid(FluidState fluid, @Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hit) {
        if (!(fluid.getFluid() instanceof FlowableFluid)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean canPlace = state.canBucketPlace(fluid.getFluid());

        if (!(state.isAir() || canPlace || block instanceof FluidFillable f && f.canFillWithFluid(player, world, pos, state, fluid.getFluid()))) {
            return hit != null && placeFluid(fluid, player, world, hit.getBlockPos().offset(hit.getSide()), null);
        }

        if (world.getDimension().ultrawarm() && !fluid.isIn(FluidTags.LAVA)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }

        if (block instanceof FluidFillable f && f.tryFillWithFluid(world, pos, state, fluid)) {
            playEmptyingSound(fluid.getFluid(), player, world, pos);
            return true;
        }

        if (!world.isClient && canPlace && !state.isLiquid()) {
            world.breakBlock(pos, true);
        }

        if (world.getBlockState(pos).isOf(fluid.getBlockState().getBlock())) {
            if (world.setBlockState(pos, fluid.getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD) || state.getFluidState().isStill()) {
               playEmptyingSound(fluid.getFluid(), player, world, pos);
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
