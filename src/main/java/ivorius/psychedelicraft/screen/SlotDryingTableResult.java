/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.screen;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SlotDryingTableResult extends Slot {
    private final PlayerEntity player;
    private int amount;

    public SlotDryingTableResult(PlayerEntity player, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (hasStack()) {
            this.amount += Math.min(amount, getStack().getCount());
        }
        return super.takeStack(amount);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        onCrafted(stack);
        super.onTakeItem(player, stack);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        onCrafted(stack);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        stack.onCraft(this.player.getWorld(), this.player, this.amount);

        if (player instanceof ServerPlayerEntity spe) {
            int amount = this.amount;
            float xpPerItem = 5;
            int xp;

            if (xpPerItem == 0) {
                amount = 0;
            } else if (xpPerItem < 1) {
                xp = MathHelper.floor(amount * xpPerItem);

                // bonus experience
                if (xp < MathHelper.ceil(amount * xpPerItem) && (float) Math.random() < amount * xpPerItem - xp) {
                    ++xp;
                }

                amount = xp;
            }

            ExperienceOrbEntity.spawn(spe.getServerWorld(), player.getPos(), amount);
        }

        this.amount = 0;
    }
}
