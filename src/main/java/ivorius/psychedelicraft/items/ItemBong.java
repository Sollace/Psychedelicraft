/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by calebmanley on 4/05/2014.
 *
 * Updated by Sollace on Jan 1 2023
 */
public class ItemBong extends Item {
    public final ArrayList<Consumable> consumables = new ArrayList<>();

    public ItemBong(Settings settings) {
        super(settings);
    }

    public void addConsumable(Consumable consumable) {
        consumables.add(consumable);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            Consumable usedConsumable = getUsedConsumable(player);
            if (usedConsumable != null && player.getInventory().contains(usedConsumable.consumedItem)) {
                // TODO: (Sollace) check for possible client desync
                player.getInventory().removeOne(usedConsumable.consumedItem);
                DrugProperties.of(entity).ifPresent(drugProperties -> {
                    for (DrugInfluence influence : usedConsumable.drugInfluences) {
                        drugProperties.addToDrug(influence.clone());
                    }

                    stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                    drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), usedConsumable.smokeColor);
                });
            }
        }

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (DrugProperties.of(player).timeBreathingSmoke <= 0 && getUsedConsumable(player) != null) {
            return TypedActionResult.consume(player.getStackInHand(hand));
        }

        return TypedActionResult.fail(ItemStack.EMPTY);
    }

    public Consumable getUsedConsumable(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return null;
        }

        for (Consumable consumable : consumables) {
            if (((PlayerEntity)entity).getInventory().contains(consumable.consumedItem)) {
                return consumable;
            }
        }

        return null;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 30;
    }

    public record Consumable (
            ItemStack consumedItem,
            DrugInfluence[] drugInfluences,
            float[] smokeColor
    ) {
        public Consumable(ItemStack consumedItem, DrugInfluence...drugInfluences) {
            this(consumedItem, drugInfluences, new float[]{ 1, 1, 1 });
        }
    }
}
