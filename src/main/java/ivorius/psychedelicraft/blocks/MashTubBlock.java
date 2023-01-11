/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlock extends BlockWithFluid<MashTubBlockEntity> {
    public static final int SIZE = 15;
    public static final int BORDER_SIZE = 1;
    public static final int HEIGHT = 16;

    public static final BooleanProperty MASTER = BooleanProperty.of("master");

    // TODO: (Sollace) MushTub is a 3x3 multi-block and this voxel shape reflects that
    //           x from -15 to +30
    private static final VoxelShape SHAPE = VoxelShapes.union(
            createShape(-16, -0.5F, -16, 32, 16,  1),
            createShape(-16, -0.5F,  15, 32, 16,  1),
            createShape( 15, -0.5F, -16,  1, 16, 32),
            createShape(-16, -0.5F, -16,  1, 16, 32),
            createShape(-16, -0.5F, -16, 32,  1, 32)
    );
    private static VoxelShape createShape(double x, double y, double z, double width, double height, double depth) {
        return Block.createCuboidShape(x, y, z, x + width, y + height, z + depth);
    }

    public MashTubBlock(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(MASTER)) {
            return SHAPE;
        }

        return BlockPos.findClosest(pos, 1, 0, p -> {
           BlockState s = world.getBlockState(p);
           return s.isOf(this) && s.get(MASTER);
        }).map(p -> SHAPE.offset(p.getX()-pos.getX(), 0, p.getZ()-pos.getZ())).orElseGet(() -> {
            boolean east = world.getBlockState(pos.east()).isOf(this);
            boolean west = world.getBlockState(pos.west()).isOf(this);

            boolean north = world.getBlockState(pos.north()).isOf(this);
            boolean south = world.getBlockState(pos.south()).isOf(this);

            return VoxelShapes.union(
                    north ? VoxelShapes.empty() : createShape(0, -0.5F, 0, 16, 16,  1),
                    south ? VoxelShapes.empty() : createShape(0, -0.5F, 15, 16, 16,  1),
                    east ? VoxelShapes.empty() : createShape(15, -0.5F, 0,  1, 16, 16),
                    west ? VoxelShapes.empty() : createShape(0, -0.5F, 0,  1, 16, 16),
                    createShape(0, -0.5F, 0, 16,  1, 16)
            );
        });
/*
        boolean east = world.getBlockState(pos.east()).isOf(this);
        boolean west = world.getBlockState(pos.west()).isOf(this);

        boolean north = world.getBlockState(pos.north()).isOf(this);
        boolean south = world.getBlockState(pos.south()).isOf(this);

        return VoxelShapes.union(
                north ? VoxelShapes.empty() : createShape(0, -0.5F, 0, 16, 16,  1),
                south ? VoxelShapes.empty() : createShape(0, -0.5F, 15, 16, 16,  1),
                east ? VoxelShapes.empty() : createShape(15, -0.5F, 0,  1, 16, 16),
                west ? VoxelShapes.empty() : createShape(0, -0.5F, 0,  1, 16, 16),
                createShape(0, -0.5F, 0, 16,  1, 16)
        );*/
    }

    @Override
    protected BlockEntityType<MashTubBlockEntity> getBlockEntityType() {
        return PSBlockEntities.MASH_TUB;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        return super.getPlacementState(ctx).with(MASTER, !BlockPos.findClosest(ctx.getBlockPos(), 1, 0, p -> {
            return world.getBlockState(p).isOf(this);
        }).isPresent());
    }

    @Override
    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, MashTubBlockEntity blockEntity) {
        if (!blockEntity.solidContents.isEmpty()) {
            Block.dropStack(world, pos, blockEntity.solidContents);
            blockEntity.solidContents = ItemStack.EMPTY;
            blockEntity.markDirty();
            if (!world.isClient) {
                ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MASTER);
    }
}
