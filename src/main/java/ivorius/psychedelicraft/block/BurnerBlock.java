/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.contents.EmptyContents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BurnerBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(5, 2, 5);
    private static final Map<Identifier, VoxelShape> SHAPE_CACHE = new HashMap<>(Map.of(EmptyContents.ID, SHAPE));

    public static final BooleanProperty LIT = Properties.LIT;

    public BurnerBlock(Settings settings) {
        super(settings
                .nonOpaque()
                .dynamicBounds()
                .emissiveLighting((state, world, pos) -> state.getOrEmpty(LIT).orElse(false))
        );
        setDefaultState(getDefaultState().with(LIT, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BurnerBlockEntity be = world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).orElse(null);
        return be == null || be.getContents().getId() == EmptyContents.ID
                ? SHAPE
                : SHAPE_CACHE.computeIfAbsent(be.getContents().getId(), id -> VoxelShapes.union(SHAPE, be.getContents().getOutlineShape()));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            world.addParticle(ParticleTypes.SMOKE,
                    world.random.nextTriangular(pos.getX() + 0.5, 0.2),
                    world.random.nextTriangular(pos.getY() + 0.2, 0.2),
                    world.random.nextTriangular(pos.getZ() + 0.5, 0.2),
                    0, 0, 0);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isIn(ItemTags.CREEPER_IGNITERS) && state.get(LIT)) {
            SoundEvent sound = stack.isOf(Items.FIRE_CHARGE) ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ITEM_FLINTANDSTEEL_USE;
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            if (!world.isClient) {
                if (!stack.isDamageable()) {
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                } else {
                    stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                }
                world.setBlockState(pos, state.cycle(LIT));
            }
            return ActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).filter(data -> data.interact(stack, player, hand, hit.getSide())).isPresent()) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = isReceivingPower(world, pos);
        if (powered != state.get(LIT)) {
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.cycle(LIT));
        }
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof Inventory i) {
            ItemScatterer.spawn(world, pos, i);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private boolean isReceivingPower(World world, BlockPos pos) {
        return world.isReceivingRedstonePower(pos)
            || DistilleryBlockEntity.getBlockHeat(world.getBlockState(pos.down())) > 0;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(LIT) && !entity.isSneaking() && entity.age % 10 == 0 && entity.isSupportedBy(pos)) {
            entity.damage(entity.getDamageSources().inFire(), 1);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(LIT, isReceivingPower(ctx.getWorld(), ctx.getBlockPos()));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient
                ? checkType(type, PSBlockEntities.BUNSEN_BURNER, (w, p, s, entity) -> entity.clientTick(w))
                : checkType(type, PSBlockEntities.BUNSEN_BURNER, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }
}
