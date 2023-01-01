/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;

import java.util.List;

import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SmokeableItem extends Item {
    public final List<DrugInfluence> drugEffects;

    public float[] smokeColor;

    private final int useStages;

    public SmokeableItem(Settings settings, int useStages, DrugInfluence... drugEffects) {
        this(settings, useStages, List.of(drugEffects));
    }

    protected SmokeableItem(Settings settings, int useStages, List<DrugInfluence> drugEffects) {
        super(settings);
        this.useStages = useStages;
        this.drugEffects = drugEffects;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 25;
    }

    public int getStages() {
        return useStages;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(entity);

        if (drugProperties != null) {
            for (DrugInfluence drugInfluence : drugEffects) {
                drugProperties.addToDrug(drugInfluence.clone());
            }

            drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), smokeColor);
        }

        if (stack.getDamage() < stack.getMaxDamage() - 1) {
            stack.damage(1, entity, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        } else {
            stack.decrement(1);
        }

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (DrugProperties.getDrugProperties(player).timeBreathingSmoke == 0) {
            return TypedActionResult.success(stack, world.isClient());
        }

        return TypedActionResult.pass(stack);
    }
}
