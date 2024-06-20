package ivorius.psychedelicraft.screen;

import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

/**
* Created by lukas on 08.11.14.
*/
public class DryingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    private final PropertyDelegate properties;

    public DryingTableScreenHandler(int syncId, PlayerInventory inventory, BlockPos pos) {
        this(syncId, inventory, (DryingTableBlockEntity)inventory.player.getWorld().getBlockEntity(pos));
    }

    public DryingTableScreenHandler(int syncId, PlayerInventory inventory, DryingTableBlockEntity container) {
        super(PSScreenHandlers.DRYING_TABLE, syncId);
        this.inventory = inventory;
        this.properties = container.propertyDelegate;

        addSlot(new SlotDryingTableResult(inventory.player, container, 0, 124, 35));

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                addSlot(new Slot(container, 1 + x * 3 + y, 30 + x * 18, 17 + y * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addProperties(properties);
    }

    public float getHeatRatio() {
        return properties.get(0) / 1000F;
    }

    public float getProgress() {
        return getCookingTime() == 0 ? 0 : (float)properties.get(1) / getCookingTime();
    }

    public int getCookingTime() {
        return properties.get(2);
    }

    public int getTimeRemaining() {
        float progress = properties.get(1);
        float timeRemaining = getCookingTime() - progress;
        return (int)(timeRemaining / getHeatRatio());
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
