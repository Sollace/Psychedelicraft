/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;

import org.joml.Vector3f;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SmokeableItem extends Item {
    public static Vector3f WHITE = new Vector3f(1, 1, 1);

    private final List<DrugInfluence> drugEffects;

    private final Vector3f smokeColor;

    private final int useStages;

    public SmokeableItem(Settings settings, int useStages, Vector3f smokeColor, DrugInfluence... drugEffects) {
        super(settings);
        this.smokeColor = smokeColor;
        this.useStages = useStages;
        this.drugEffects = List.of(drugEffects);
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
        return UseAction.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            drugProperties.addAll(drugEffects);
            drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), smokeColor);
        });


        if (!(entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode)) {
            if (!stack.isDamageable()) {
                stack.decrement(1);
            } else {
                stack.damage(1, entity, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
        }

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!DrugProperties.of(player).isBreathingSmoke()) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.fail(stack);
    }
}
