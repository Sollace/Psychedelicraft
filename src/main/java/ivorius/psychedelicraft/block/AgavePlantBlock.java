/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public class AgavePlantBlock extends SucculentPlantBlock {
    public static final IntProperty AGE = Properties.AGE_5;
    public static final int MAX_AGE = Properties.AGE_5_MAX;

    public static final VoxelShape[] SHAPES = {
            Block.createCuboidShape(6, 0, 6, 10,  4, 10),
            Block.createCuboidShape(6, 0, 6, 10,  8, 10),
            Block.createCuboidShape(5, 0, 5, 11, 10, 11),
            Block.createCuboidShape(5, 0, 5, 11, 10, 11),
            Block.createCuboidShape(4, 0, 4, 12, 12, 12),
            Block.createCuboidShape(2, 0, 2, 14, 14, 14)
    };

    public AgavePlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape[] getShapes() {
        return SHAPES;
    }

    @Override
    protected IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected int getGrowthRate(BlockState state) {
        return 320;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return state.get(getAgeProperty()) < getMaxAge();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
            return;
        }
        entity.slowMovement(state, new Vec3d(0.8F, 0.75F, 0.8F));
        if (!(world.isClient
                || state.get(getAgeProperty()) <= 0
                || entity.lastRenderX == entity.getX() && entity.lastRenderZ == entity.getZ()
                || entity.isSneaking())) {
            if (Math.max(
                    Math.abs(entity.getX() - entity.lastRenderX),
                    Math.abs(entity.getZ() - entity.lastRenderZ)
                ) >= 0.003F) {
                entity.damage(DamageSource.CACTUS, 1);
            }
        }
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        int age = state.get(getAgeProperty());

        if ((stack.isIn(ConventionalItemTags.SHEARS) && age >= 1)) {
            stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            dropStack(world, pos, new ItemStack(PSItems.AGAVE_LEAF, 1 + world.random.nextInt(2)));

            state = state.with(getAgeProperty(), age - 1);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));

            if (stack.isOf(Items.BONE_MEAL)) {
                return ActionResult.PASS;
            }

            world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS,
                    1,
                    0.8F + world.random.nextFloat() * 0.4F
            );
            return ActionResult.success(world.isClient);
        }
        if (stack.isEmpty()) {
            player.damage(DamageSource.CACTUS, 1);
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!isAppropriateTool(player.getStackInHand(Hand.MAIN_HAND))) {
            player.damage(DamageSource.CACTUS, 1);
        }
    }

    private boolean isAppropriateTool(ItemStack stack) {
        return stack.isIn(ConventionalItemTags.PICKAXES)
                || stack.isIn(ConventionalItemTags.SHOVELS)
                || stack.isIn(ConventionalItemTags.SWORDS)
                || stack.isIn(ConventionalItemTags.AXES)
                || stack.isIn(ConventionalItemTags.HOES)
                || stack.isIn(ConventionalItemTags.SHEARS);
    }
}
