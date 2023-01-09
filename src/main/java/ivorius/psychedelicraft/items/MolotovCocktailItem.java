/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.entities.EntityMolotovCocktail;
import ivorius.psychedelicraft.fluids.ConsumableFluid.ConsumptionType;
import ivorius.psychedelicraft.fluids.Combustable;
import ivorius.psychedelicraft.fluids.SimpleFluid;
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
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        float strength = user.getItemUseTimeLeft() / (float)getMaxUseTime(itemStack);

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!world.isClient) {
            EntityMolotovCocktail projectile = new EntityMolotovCocktail(world, user);
            projectile.setItem(itemStack);
            projectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.5F * strength, 1F);
            world.spawnEntity(projectile);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public Text getName(ItemStack stack) {
        SimpleFluid fluid = getFluid(stack);

        if (!fluid.isEmpty()) {
            int quality = 0;

            if (fluid instanceof Combustable exploding) {
                float explStr = exploding.getExplosionStrength(stack) * 0.8f;
                float fireStr = exploding.getFireStrength(stack) * 0.6f;

                quality = MathHelper.clamp(MathHelper.floor((fireStr + explStr) + 0.5f), 0, 7);
            }

            return Text.translatable(getTranslationKey(stack) + ".quality." + quality + ".name");
        }

        return super.getName();
    }
}
