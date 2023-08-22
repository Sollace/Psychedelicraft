/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.screen;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
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

    static final int INVENTORY_START = 2;
    static final int INVENTORY_END = 28;
    static final int HOTBAR_START = INVENTORY_END + 1;
    static final int HOTBAR_END = HOTBAR_START + 9;

    private final Resovoir tank;

    private final T blockEntity;

    @SuppressWarnings("unchecked")
    public FluidContraptionScreenHandler(ScreenHandlerType<? extends FluidContraptionScreenHandler<T>> type, int syncId, PlayerInventory inventory, PacketByteBuf buffer) {
        this(type, syncId, inventory, (T)inventory.player.getWorld().getBlockEntity(buffer.readBlockPos()), buffer.readEnumConstant(Direction.class));
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
        @Nullable
        Slot slot = slots.get(index);

        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack originalStack = stack.copy();

        if (index < INVENTORY_START) {
            if (!insertItem(stack, HOTBAR_START, HOTBAR_END, true)
                    && !insertItem(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, originalStack);
        } if (index < HOTBAR_START) {
            if (!insertStack(stack, 0) && !insertStack(stack, 1)
                    && !insertItem(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, originalStack);
        } else {
            if (!insertStack(stack, 0)
                    && !insertStack(stack, 1)
                    && !insertItem(stack, INVENTORY_START, INVENTORY_END, false)
                    && !insertItem(stack, HOTBAR_START, Math.min(index - 1, HOTBAR_END), false)
                    && !insertItem(stack, Math.max(index + 1, HOTBAR_START), HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, originalStack);
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

        return originalStack;
    }

    private boolean insertStack(ItemStack stack, int slotIndex) {
        @Nullable
        Slot slot = slots.get(slotIndex);
        if (slot == null) {
            return false;
        }
        ItemStack currentStack = slot.getStack();
        if (!currentStack.isEmpty() || !slot.canInsert(stack)) {
            return false;
        }

        slot.setStack(stack.split(Math.min(stack.getCount(), slot.getMaxItemCount(stack))));
        slot.markDirty();
        return true;
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
