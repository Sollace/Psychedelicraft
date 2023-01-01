/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.entities.EntityMolotovCocktail;
import ivorius.psychedelicraft.fluids.ConsumableFluid.ConsumptionType;
import ivorius.psychedelicraft.fluids.ExplodingFluid;
import ivorius.psychedelicraft.fluids.FluidHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class ItemMolotovCocktail extends DrinkableItem {
    public static ExplodingFluid getExplodingFluid(ItemStack stack) {
        if (stack.getItem() instanceof FluidContainerItem container
            && container.getFluid(stack) instanceof ExplodingFluid exploder) {
            return exploder;
        }

        return null;
    }

    public ItemMolotovCocktail(Settings settings, int capacity) {
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
    public void onStoppedUsing(ItemStack stack, World world, PlayerEntity player, int timeUsed) {
        stack.decrement(1);
        if (stack.isEmpty()) {
            player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
        }

        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote)
        {
            float strength = MathHelper.clamp((getMaxUseTime(stack) - timeUsed) / 30F, 0, 1);

            EntityMolotovCocktail molotovCocktail = new EntityMolotovCocktail(world, player);

            molotovCocktail.molotovStack = stack.copy();
            molotovCocktail.molotovStack.stackSize = 1;
            molotovCocktail.motionX *= partUsed;
            molotovCocktail.motionY *= partUsed;
            molotovCocktail.motionZ *= partUsed;

            world.spawnEntityInWorld(molotovCocktail);
        }
    }

    private TypedActionResult<ItemStack> launch(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!world.isClient) {
            EntityMolotovCocktail snowballEntity = new EntityMolotovCocktail(world, user);
            snowballEntity.setItem(itemStack);
            snowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
            world.spawnEntity(snowballEntity);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);

        if (fluidStack != null)
        {
            int quality = 0;

            if (fluidStack.getFluid() instanceof ExplodingFluid)
            {
                ExplodingFluid explodingFluid = (ExplodingFluid) fluidStack.getFluid();
                float explStr = explodingFluid.explosionStrength(fluidStack) * 0.8f;
                float fireStr = explodingFluid.fireStrength(fluidStack) * 0.6f;

                quality = MathHelper.clamp_int(MathHelper.floor_float((fireStr + explStr) + 0.5f), 0, 7);
            }

            return StatCollector.translateToLocalFormatted(this.getUnlocalizedNameInefficiently(stack) + ".quality" + quality + ".name");
        }

        return StatCollector.translateToLocalFormatted(this.getUnlocalizedNameInefficiently(stack) + ".empty.name");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean aBool)
    {
        super.addInformation(stack, player, list, aBool);

        FluidStack fluidStack = getFluid(stack);

        if (fluidStack != null)
            list.add(fluidStack.getLocalizedName());
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        for (int dmg = 0; dmg < 16; dmg++)
        {
            list.add(new ItemStack(item, 1, dmg));

            for (FluidStack fluidStack : FluidHelper.allFluids(ExplodingFluid.SUBTYPE, capacity))
            {
                ItemStack stack = new ItemStack(item, 1, dmg);
                fill(stack, fluidStack, true);
                list.add(stack);
            }
        }
    }
}
