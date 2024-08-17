/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class BurnerBlock extends BlockWithEntity implements PipeInsertable {
    public static final MapCodec<BurnerBlock> CODEC = createCodec(BurnerBlock::new);
    private static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(5, 2, 5);

    public static final BooleanProperty LIT = Properties.LIT;

    public BurnerBlock(Settings settings) {
        super(settings
                .nonOpaque()
                .emissiveLighting((state, world, pos) -> state.getOrEmpty(LIT).orElse(false))
        );
        setDefaultState(getDefaultState().with(LIT, false));
    }

    @Override
    protected MapCodec<? extends BurnerBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
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
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isIn(ItemTags.CREEPER_IGNITERS) && state.get(LIT)) {
            SoundEvent sound = stack.isOf(Items.FIRE_CHARGE) ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ITEM_FLINTANDSTEEL_USE;
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            if (!world.isClient) {
                if (!stack.isDamageable()) {
                    stack.decrementUnlessCreative(1, player);
                } else {
                    stack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                world.setBlockState(pos, state.cycle(LIT));
            }
            return ItemActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).filter(data -> data.interact(stack, player, hand, hit.getSide())).isPresent()) {
            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        if (powered != state.get(LIT)) {
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.cycle(LIT));
        }
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return input && direction == Direction.UP;
    }

    @Override
    public int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        if (direction != Direction.DOWN) {
            return SPILL_STATUS;
        }
        return world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).map(data -> {
            return data.getContents().tryInsert(world, state, pos, direction, fluids);
        }).orElse(SPILL_STATUS);
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockWithFluid.appendDroppedStacks(ItemStack.EMPTY, state, builder);
        return super.getDroppedStacks(state, builder);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(LIT) && !entity.isSneaking() && entity.age % 10 == 0) {
            entity.damage(entity.getDamageSources().inFire(), 1);
        }
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient ? null : validateTicker(type, PSBlockEntities.BUNSEN_BURNER, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

}
