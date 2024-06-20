/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class EdibleItem extends Item {
    public static final FoodComponent NON_FILLING_EDIBLE = new FoodComponent.Builder().nutrition(0).saturationModifier(0.1F).alwaysEdible().build();
    public static final FoodComponent HASH_MUFFIN = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1F).alwaysEdible().build();
    public static final FoodComponent TOMATO = new FoodComponent.Builder().nutrition(1).saturationModifier(1.9F).alwaysEdible().build();

    private final DrugInfluence influence;

    public EdibleItem(Settings settings, DrugInfluence influence) {
        super(settings);
        this.influence = influence;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack remainder = super.finishUsing(stack, world, user);

        if (stack.get(DataComponentTypes.FOOD) == null && (!(user instanceof PlayerEntity) || ((PlayerEntity)user).isCreative())) {
            remainder.decrement(1);
        }

        DrugProperties.of(user).ifPresent(drugProperties -> drugProperties.addToDrug(influence));
        return remainder;
    }
}
