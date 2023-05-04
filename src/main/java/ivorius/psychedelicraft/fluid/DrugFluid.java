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
import ivorius.psychedelicraft.fluid.alcohol.FluidAppearance;
import ivorius.psychedelicraft.item.PSItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by lukas on 22.10.14.
 */
public class DrugFluid extends SimpleFluid implements ConsumableFluid, Combustable {
    protected final List<DrugInfluence> drugInfluences;
    protected final FoodComponent foodLevel;

    protected final Settings settings;

    protected final Map<String, Identifier> flowTextures = new HashMap<>();

    public DrugFluid(Identifier id, Settings settings) {
        super(id, settings);
        this.settings = settings;
        this.foodLevel = settings.foodLevel;
        this.drugInfluences = settings.drugInfluences;
    }

    @Nullable
    public FoodComponent getFoodLevel(ItemStack fluidStack) {
        return foodLevel;
    }

    public void getDrugInfluences(ItemStack fluidStack, List<DrugInfluence> list) {
        getDrugInfluencesPerLiter(fluidStack, influence -> {
            DrugInfluence clone = influence.clone();
            clone.setMaxInfluence(clone.getMaxInfluence() * FluidContainer.of(fluidStack).getLevel(fluidStack) / FluidVolumes.BUCKET);
            list.add(clone);
        });
    }

    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        drugInfluences.forEach(consumer);
    }

    @Override
    public boolean canConsume(ItemStack fluidStack, LivingEntity entity, ConsumptionType type) {
        if (type == ConsumptionType.DRINK) {
            return settings.drinkable && (
                    !(entity instanceof PlayerEntity)
                    || getFoodLevel(fluidStack) == null
                    || ((PlayerEntity) entity).getHungerManager().isNotFull()
                );
        }

        return settings.injectable;
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
        return getAlcohol(fluidStack) * FluidContainer.of(fluidStack).getLevel(fluidStack) / FluidVolumes.BUCKET * 2.0f;
    }

    @Override
    public float getExplosionStrength(ItemStack fluidStack) {
        return getAlcohol(fluidStack) * FluidContainer.of(fluidStack).getLevel(fluidStack) / FluidVolumes.BUCKET * 0.6f;
    }

    @Override
    public Optional<Identifier> getFlowTexture(ItemStack stack) {
        return Optional.ofNullable(settings.appearance.apply(stack))
                .map(FluidAppearance::still)
                .map(name -> flowTextures.computeIfAbsent(name, this::getFlowTexture));
    }

    protected Identifier getFlowTexture(String name) {
        return getId().withPath(p -> "block/fluid/" + name);
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
    public boolean isSuitableContainer(FluidContainer container) {
        return container != PSItems.WOODEN_MUG && !settings.injectable;
    }

    public static class Settings extends SimpleFluid.Settings {
        private boolean drinkable;
        private boolean injectable;

        private List<DrugInfluence> drugInfluences = new ArrayList<>();
        private FoodComponent foodLevel;

        protected Function<ItemStack, FluidAppearance> appearance = stack -> null;

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

        public Settings appearance(FluidAppearance appearance) {
            this.appearance = stack -> appearance;
            return this;
        }
    }
}
