/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

/**
 * Created by Sollace on Jan 1 2023
 */
public class DrinkableItem extends Item implements FluidContainer {
    public static final int FLUID_PER_DRINKING = FluidVolumes.BUCKET / 4;
    public static final int DEFAULT_MAX_USE_TIME = (int)(1.6F * 20);

    private final int capacity;

    private final int consumptionVolume;
    private final int consumptionTime;
    private final ConsumableFluid.ConsumptionType consumptionType;

    public DrinkableItem(Settings settings, int capacity, int consumptionVolume, int consumptionTime, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.maxCount(1));
        this.capacity = capacity;
        this.consumptionVolume = Math.min(capacity, consumptionVolume);
        this.consumptionTime = consumptionTime;
        this.consumptionType = consumptionType;
    }

    @Override
    public int getMaxCapacity() {
        return capacity;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return getFluid(stack).isEmpty() ? UseAction.NONE
                : consumptionType == ConsumableFluid.ConsumptionType.DRINK
                ? UseAction.DRINK
                : UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        return ConsumableFluid.consume(stack, entity, consumptionVolume, !(entity instanceof PlayerEntity p && p.isCreative()), consumptionType);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (ConsumableFluid.canConsume(stack, player, consumptionVolume, consumptionType)) {
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
        SimpleFluid fluid = getFluid(stack);

        if (!fluid.isEmpty()) {
            return Text.translatable(getTranslationKey() + ".filled", fluid.getName(stack));
        }

        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("psychedelicraft.drink.levels", getLevel(stack), getMaxCapacity(stack)).formatted(Formatting.GRAY));

        SimpleFluid fluid = getFluid(stack);
        fluid.appendTooltip(stack, tooltip, type);
        if (type.isAdvanced()) {
            tooltip.add(Text.literal("contents: " + fluid.getId().toString()).formatted(Formatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !getFluid(stack).isEmpty() && getFillPercentage(stack) < 1;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int)(ITEM_BAR_STEPS * getFillPercentage(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }
}
