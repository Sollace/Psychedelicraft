/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

/**
 * Created by Sollace on Jan 1 2023
 */
public class DrinkableItem extends Item {
    public static final int FLUID_PER_DRINKING = FluidVolumes.BUCKET / 4;
    public static final int FLUID_PER_INJECTION = FluidVolumes.SYRINGE;
    public static final int DEFAULT_MAX_USE_TIME = (int)(1.6F * 20);

    private final int consumptionVolume;
    private final int consumptionTime;
    private final ConsumableFluid.ConsumptionType consumptionType;

    public DrinkableItem(Settings settings, int consumptionVolume, int consumptionTime, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.maxCount(1));
        this.consumptionVolume = consumptionVolume;
        this.consumptionTime = consumptionTime;
        this.consumptionType = consumptionType;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return ItemFluids.of(stack).isEmpty() ? UseAction.NONE
                : consumptionType == ConsumableFluid.ConsumptionType.DRINK
                ? UseAction.DRINK
                : UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        return use(stack, world, entity, entity);
    }

    protected boolean canUse(ItemStack stack, World world, LivingEntity entity) {
        return ConsumableFluid.canConsume(stack, entity, consumptionVolume, consumptionType);
    }

    protected ItemStack use(ItemStack stack, World world, LivingEntity entity, LivingEntity user) {
        @Nullable SoundEvent sound = getUseSound();
        if (sound != null) {
            entity.playSound(sound, 2, (float)entity.getRandom().nextTriangular(0.5, 0.2));
        }
        return ConsumableFluid.consume(stack, entity, consumptionVolume, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(user), consumptionType);
    }

    @Nullable
    protected SoundEvent getUseSound() {
        return null;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (canUse(stack, world, player)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return PlacedDrinksBlock.tryPlace(context);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return consumptionTime;
    }

    @Override
    public Text getName(ItemStack stack) {
        ItemFluids fluid = ItemFluids.of(stack);

        if (!fluid.isEmpty()) {
            return Text.translatable(getTranslationKey() + ".filled", fluid.fluid().getName(fluid));
        }

        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        FluidCapacity.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !ItemFluids.of(stack).isEmpty() && FluidCapacity.getPercentage(stack) < 1;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int)(ITEM_BAR_STEPS * FluidCapacity.getPercentage(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }
}
