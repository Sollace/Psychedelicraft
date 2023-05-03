package ivorius.psychedelicraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public class NightshadeBlock extends PlantBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_7;
    public static final int MAX_AGE = Properties.AGE_7_MAX;

    private static final VoxelShape[] SHAPES = {
            ShapeUtil.createCenteredShape(1, 2, 1),
            ShapeUtil.createCenteredShape(1.5, 4, 1.5),
            ShapeUtil.createCenteredShape(2, 6, 2),
            ShapeUtil.createCenteredShape(3, 9, 3),
            ShapeUtil.createCenteredShape(6, 10, 6),
            ShapeUtil.createCenteredShape(6, 14, 6),
            ShapeUtil.createCenteredShape(7, 15, 7),
            ShapeUtil.createCenteredShape(7, 15, 7)
    };

    private final ItemConvertible fruit;
    private final ItemConvertible leaf;

    public NightshadeBlock(Settings settings, ItemConvertible fruit, ItemConvertible leaf) {
        super(settings);
        this.fruit = fruit;
        this.leaf = leaf;
        setDefaultState(getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(AGE)];
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(AGE) < MAX_AGE && random.nextInt(5) == 0 && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            state = state.cycle(AGE);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || state.get(AGE) < 3 || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
            return;
        }
        entity.slowMovement(state, new Vec3d(0.8f, 0.75, 0.8f));
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        ItemStack stack = player.getStackInHand(hand);
        int age = state.get(AGE);

        if ((stack.isOf(Items.SHEARS) && age >= 1) || (stack.isOf(Items.BONE_MEAL) && age == MAX_AGE)) {
            stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            dropStack(world, pos, new ItemStack((age == MAX_AGE ? fruit : leaf).asItem(), 1 + world.random.nextInt(2)));

            state = state.with(AGE, age - 1);
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

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return state.get(AGE) < MAX_AGE;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int i = Math.min(MAX_AGE, state.get(AGE) + 1);
        world.setBlockState(pos, state.with(AGE, i), Block.NOTIFY_LISTENERS);
    }
}
