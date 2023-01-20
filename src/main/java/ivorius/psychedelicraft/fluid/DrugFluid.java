/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.item.PSItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 22.10.14.
 */
public class DrugFluid extends SimpleFluid implements ConsumableFluid, Combustable {
    protected final List<DrugInfluence> drugInfluences;
    protected final FoodComponent foodLevel;

    private final boolean drinkable;
    private final boolean injectable;

    public DrugFluid(Identifier id, Settings settings) {
        super(id, settings);
        drinkable = settings.drinkable;
        injectable = settings.injectable;
        drugInfluences = settings.drugInfluences;
        foodLevel = settings.foodLevel;
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public boolean isInjectable() {
        return injectable;
    }

    @Nullable
    public FoodComponent getFoodLevel(ItemStack fluidStack) {
        return foodLevel;
    }

    public void getDrugInfluences(ItemStack fluidStack, List<DrugInfluence> list) {
        List<DrugInfluence> influencesPerLiter = new ArrayList<>();
        getDrugInfluencesPerLiter(fluidStack, influencesPerLiter);

        for (DrugInfluence influence : influencesPerLiter) {
            DrugInfluence clone = influence.clone();
            clone.setMaxInfluence(clone.getMaxInfluence() * fluidStack.getCount() / FluidContainerItem.of(fluidStack, FluidContainerItem.BUCKET).getMaxCapacity(fluidStack));
            list.add(clone);
        }
    }

    public void getDrugInfluencesPerLiter(ItemStack fluidStack, List<DrugInfluence> list) {
        list.addAll(drugInfluences);
    }

    @Override
    public boolean canConsume(ItemStack fluidStack, LivingEntity entity, ConsumptionType type) {
        if (type == ConsumptionType.DRINK) {
            return isDrinkable() && (
                    !(entity instanceof PlayerEntity)
                    || getFoodLevel(fluidStack) == null
                    || ((PlayerEntity) entity).getHungerManager().isNotFull()
                );
        }

        return isInjectable();
    }

    @Override
    public void consume(ItemStack fluidStack, LivingEntity entity, ConsumptionType type) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            List<DrugInfluence> drugInfluences = new ArrayList<>();
            getDrugInfluences(fluidStack, drugInfluences);
            drugProperties.addAll(drugInfluences);
        });

        if (type == ConsumptionType.DRINK) {
            if (foodLevel != null && entity instanceof PlayerEntity player) {
                player.getHungerManager().add(foodLevel.getHunger(), foodLevel.getSaturationModifier());
            }
        }
    }

    @Override
    public float getFireStrength(ItemStack fluidStack) {
        return getAlcohol(fluidStack) * fluidStack.getCount() / FluidContainerItem.of(fluidStack, FluidContainerItem.BUCKET).getMaxCapacity(fluidStack) * 2.0f;
    }

    @Override
    public float getExplosionStrength(ItemStack fluidStack) {
        return getAlcohol(fluidStack) * fluidStack.getCount() / FluidContainerItem.of(fluidStack, FluidContainerItem.BUCKET).getMaxCapacity(fluidStack) * 0.6f;
    }

    private float getAlcohol(ItemStack fluidStack) {
        float alcohol = 0.0f;

        List<DrugInfluence> drugInfluences = new ArrayList<>();
        getDrugInfluences(fluidStack, drugInfluences);

        for (DrugInfluence drugInfluence : drugInfluences) {
            if (drugInfluence.isOf(DrugType.ALCOHOL)) {
                alcohol += drugInfluence.getMaxInfluence();
            }
        }
        return MathHelper.clamp(alcohol, 0.0f, 1.0f);
    }

    @Override
    public boolean isSuitableContainer(FluidContainerItem container) {
        return container != PSItems.WOODEN_MUG && !isInjectable();
    }

    public static class Settings extends SimpleFluid.Settings {
        private boolean drinkable;
        private boolean injectable;

        private List<DrugInfluence> drugInfluences = new ArrayList<>();
        private FoodComponent foodLevel;

        public Settings drinkable() {
            drinkable = true;
            return this;
        }

        public Settings injectable() {
            injectable = true;
            return this;
        }

        public Settings influence(DrugInfluence... influences) {
            drugInfluences.addAll(List.of(influences));
            return this;
        }

        public Settings food(FoodComponent food) {
            this.foodLevel = food;
            return this;
        }
    }
}
