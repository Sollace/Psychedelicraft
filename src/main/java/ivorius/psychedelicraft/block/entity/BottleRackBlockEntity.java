package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;

import java.util.*;

import ivorius.psychedelicraft.items.PSItems;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class BottleRackBlockEntity extends BlockEntityWithInventory {
    private static final int[] SLOTS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    public BottleRackBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BOTTLE_RACK, pos, state, 9);
        // TODO: (Sollace) Bottle rack technically also has a frame and beams that shouldn't be recognised as slots
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        // TODO: (Sollace) Tags
        return stack.isOf(PSItems.MOLOTOV_COCKTAIL) || stack.isOf(PSItems.BOTTLE);
        //return stack.isIn(PSTags.BOTTLES);
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return direction.getAxis() == Axis.Y ? NO_SLOTS : SLOTS;
    }

    public ActionResult insertItem(ItemStack stack, BlockHitResult hit, Direction facing) {
        return getHitPos(hit, facing).map(pos -> {
            int slot = getSlot(pos);
            if (getStack(slot).isEmpty() && isValid(slot, stack)) {
                setStack(slot, stack);
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }).orElse(ActionResult.PASS);
    }

    public TypedActionResult<ItemStack> extractItem(BlockHitResult hit, Direction facing) {
        return getHitPos(hit, facing).map(pos -> {
            return TypedActionResult.success(removeStack(getSlot(pos)));
        }).orElse(TypedActionResult.fail(ItemStack.EMPTY));
    }

    private static int getSlot(Vec2f pos) {
        int x = (int)(pos.x / 3F);
        int y = (int)(pos.y / 3F);
        return x + (y * 3);
    }

    private static Optional<Vec2f> getHitPos(BlockHitResult hit, Direction facing) {
        Direction direction = hit.getSide();
        if (facing != direction) {
            return Optional.empty();
        }

        BlockPos pos = hit.getBlockPos().offset(direction);
        Vec3d vec3d = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        float d = (float)vec3d.getX();
        float e = (float)vec3d.getY();
        float f = (float)vec3d.getZ();
        return switch (direction) {
            default -> throw new IncompatibleClassChangeError();
            case NORTH -> Optional.of(new Vec2f(1 - d, e));
            case SOUTH -> Optional.of(new Vec2f(d, e));
            case WEST -> Optional.of(new Vec2f(f, e));
            case EAST -> Optional.of(new Vec2f(1 - f, e));
            case DOWN, UP -> Optional.empty();
        };
    }
}
