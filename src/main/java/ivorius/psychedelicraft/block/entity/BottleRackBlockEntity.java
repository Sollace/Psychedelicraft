package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;

import java.util.*;

import ivorius.psychedelicraft.PSTags;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class BottleRackBlockEntity extends BlockEntityWithInventory {
    private static final int[] SLOTS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    public BottleRackBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BOTTLE_RACK, pos, state, 9);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.isIn(PSTags.Items.BOTTLES);
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return direction.getAxis() == Axis.Y ? NO_SLOTS : SLOTS;
    }

    public ActionResult insertItem(ItemStack stack, BlockHitResult hit, Direction facing) {
        return getHitPos(hit, facing).map(pos -> {
            int slot = getSlot(pos);
            if (slot >= 0 && slot < 9 && getStack(slot).isEmpty() && isValid(slot, stack)) {
                setStack(slot, stack.split(1));
                getWorld().playSound(null, getPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1, 1.5F);
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }).orElse(ActionResult.PASS);
    }

    public TypedActionResult<ItemStack> extractItem(BlockHitResult hit, Direction facing) {
        return getHitPos(hit, facing).map(pos -> {
            getWorld().playSound(null, getPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1, 1);
            return TypedActionResult.success(removeStack(getSlot(pos)));
        }).orElse(TypedActionResult.fail(ItemStack.EMPTY));
    }

    private static int getSlot(Vec2f pos) {
        int x = (int)(pos.x * 3F);
        int y = (int)(pos.y * 3F);
        return x + (y * 3);
    }

    private static Optional<Vec2f> getHitPos(BlockHitResult hit, Direction facing) {
        Direction direction = hit.getSide();

        if (facing != direction) {
            if (direction.getAxis() == Axis.Y) {
                BlockPos pos = hit.getBlockPos();
                Vec3d relativePos = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
                float x = (float)relativePos.getX();
                float y = (float)relativePos.getY();
                float z = (float)relativePos.getZ();

                return switch (facing) {
                    default -> throw new RuntimeException();
                    case NORTH -> Optional.of(new Vec2f(1 - x, 1 - y));
                    case SOUTH -> Optional.of(new Vec2f(    x, 1 - y));
                    case WEST  -> Optional.of(new Vec2f(    z, 1 - y));
                    case EAST  -> Optional.of(new Vec2f(1 - z, 1 - y));
                    case DOWN, UP -> Optional.empty();
                };
            }

            return Optional.empty();
        }

        BlockPos pos = hit.getBlockPos().offset(direction);
        Vec3d relativePos = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        float x = (float)relativePos.getX();
        float y = (float)relativePos.getY();
        float z = (float)relativePos.getZ();
        return switch (direction) {
            default -> throw new RuntimeException();
            case NORTH -> Optional.of(new Vec2f(1 - x, 1 - y));
            case SOUTH -> Optional.of(new Vec2f(    x, 1 - y));
            case WEST  -> Optional.of(new Vec2f(    z, 1 - y));
            case EAST  -> Optional.of(new Vec2f(1 - z, 1 - y));
            case DOWN, UP -> Optional.empty();
        };
    }
}
