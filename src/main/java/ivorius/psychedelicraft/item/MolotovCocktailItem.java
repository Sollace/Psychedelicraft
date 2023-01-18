/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.MolotovCocktailEntity;
import ivorius.psychedelicraft.fluid.Combustable;
import ivorius.psychedelicraft.fluid.ConsumableFluid.ConsumptionType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MolotovCocktailItem extends DrinkableItem {
    public MolotovCocktailItem(Settings settings, int capacity) {
        super(settings, capacity, 0, ConsumptionType.DRINK);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 7200;
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        float strength = user.getItemUseTimeLeft() / (float)getMaxUseTime(stack);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!world.isClient) {
            MolotovCocktailEntity projectile = new MolotovCocktailEntity(world, user);
            projectile.setItem(stack);
            projectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 0.5F * strength, 1F);
            world.spawnEntity(projectile);
        }

        if (user instanceof PlayerEntity player) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (getFluid(stack).isEmpty()) {
            return Text.translatable(getTranslationKey(stack) + ".empty");
        }

        return Text.translatable(getTranslationKey(stack) + ".quality." + getQuality(stack));
    }

    private int getQuality(ItemStack stack) {
        if (getFluid(stack) instanceof Combustable exploding) {
            float explStr = exploding.getExplosionStrength(stack) * 0.8f;
            float fireStr = exploding.getFireStrength(stack) * 0.6f;

            return MathHelper.clamp(MathHelper.floor((fireStr + explStr) + 0.5f), 0, 7);
        }

        return 0;
    }
}
