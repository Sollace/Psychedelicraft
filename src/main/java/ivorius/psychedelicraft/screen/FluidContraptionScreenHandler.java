/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.screen;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.Resovoir;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;

/**
 * Created by lukas on 26.10.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class FluidContraptionScreenHandler<T extends FlaskBlockEntity> extends ScreenHandler {
    private final Resovoir tank;

    private final T blockEntity;

    @SuppressWarnings("unchecked")
    public FluidContraptionScreenHandler(ScreenHandlerType<? extends FluidContraptionScreenHandler<T>> type, int syncId, PlayerInventory inventory, PacketByteBuf buffer) {
        this(type, syncId, inventory, (T)inventory.player.world.getBlockEntity(buffer.readBlockPos()), buffer.readEnumConstant(Direction.class));
    }

    public FluidContraptionScreenHandler(ScreenHandlerType<? extends FluidContraptionScreenHandler<T>> type, int syncId, PlayerInventory inventory, T blockEntity, Direction direction) {
        super(type, syncId);
        this.tank = blockEntity.getTank(direction);
        this.blockEntity = blockEntity;
        addSlot(new InputSlot(blockEntity.ioInventory, 0, 21, 20));
        addSlot(new InputSlot(blockEntity.ioInventory, 1, 123, 61));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            addSlot(new Slot(inventory, x, 8 + x * 18, 142));
        }

        addProperties(blockEntity.propertyDelegate);
    }

    public Resovoir getTank() {
        return tank;
    }

    public T getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return tank.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack originalStack = null;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            originalStack = stack.copy();

            if (index == 0) {
                if (!insertItem(stack, 2, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!insertItem(stack, 0, 2, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stack);
        }

        return originalStack;
    }

    final class InputSlot extends Slot {
        public InputSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public ItemStack takeStack(int amount) {
            ItemStack stack = super.takeStack(amount);
            blockEntity.onContentsExternallyChanged(getIndex());
            return stack;
        }

        @Override
        public ItemStack insertStack(ItemStack stack, int count) {
            ItemStack removed = super.insertStack(stack, count);
            blockEntity.onContentsExternallyChanged(getIndex());
            return removed;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return inventory.isValid(getIndex(), stack);
        }
    }

}
