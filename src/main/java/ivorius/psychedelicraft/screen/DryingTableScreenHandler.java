package ivorius.psychedelicraft.screen;

import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
* Created by lukas on 08.11.14.
*/
public class DryingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    public final DryingTableBlockEntity entity;

    public DryingTableScreenHandler(int syncId, PlayerInventory inventory, DryingTableBlockEntity container) {
        super(null, syncId);
        this.inventory = inventory;
        this.entity = container;

        addSlot(new SlotDryingTableResult(inventory.player, container, 0, 124, 35));

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                this.addSlot(new Slot(container, 1 + x * 3 + y, 30 + x * 18, 17 + y * 18));
            }
        }

        int var3;

        for (var3 = 0; var3 < 3; ++var3) {
            for (int var4 = 0; var4 < 9; ++var4) {
                this.addSlot(new Slot(inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3) {
            this.addSlot(new Slot(inventory, var3, 8 + var3 * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasStack()) {
            ItemStack stack = slot.getStack();
            originalStack = stack.copy();

            if (index < 10) {
                if (!insertItem(stack, 10, 46, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(stack, originalStack);
            } else if (index >= 10 && index < 37) {
                if (!insertItem(stack, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 37 && index < 46) {
                if (!insertItem(stack, 10, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stack, 10, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }

        return originalStack;
    }
}
