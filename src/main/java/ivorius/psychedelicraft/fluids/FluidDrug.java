/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 22.10.14.
 */
public class FluidDrug extends FluidSimple implements FluidWithTypes, DrinkableFluid, ConsumableFluid, ExplodingFluid, TranslucentFluid
{
    protected final List<DrugInfluence> drugInfluences = new ArrayList<>();
    protected Pair<Integer, Float> foodLevel;

    protected boolean drinkable;
    protected boolean injectable;

    public FluidDrug(String fluidName)
    {
        super(fluidName);
    }

    public FluidDrug(String fluidName, DrugInfluence... influences)
    {
        super(fluidName);
        Collections.addAll(drugInfluences, influences);
    }

    public boolean isDrinkable()
    {
        return drinkable;
    }

    public void setDrinkable(boolean drinkable)
    {
        this.drinkable = drinkable;
    }

    public boolean isInjectable()
    {
        return injectable;
    }

    public void setInjectable(boolean injectable)
    {
        this.injectable = injectable;
    }

    public Pair<Integer, Float> getFoodLevel(FluidStack fluidStack)
    {
        return foodLevel;
    }

    public void setFoodLevel(Pair<Integer, Float> foodLevel)
    {
        this.foodLevel = foodLevel;
    }

    public void getDrugInfluences(FluidStack fluidStack, List<DrugInfluence> list)
    {
        List<DrugInfluence> influencesPerLiter = new ArrayList<>();
        getDrugInfluencesPerLiter(fluidStack, influencesPerLiter);

        for (DrugInfluence influence : influencesPerLiter)
        {
            DrugInfluence clone = influence.clone();
            clone.setMaxInfluence(clone.getMaxInfluence() * (double) fluidStack.amount / (double) FluidHelper.MILLIBUCKETS_PER_LITER);
            list.add(clone);
        }
    }

    public void getDrugInfluencesPerLiter(FluidStack fluidStack, List<DrugInfluence> list)
    {
        list.addAll(drugInfluences);
    }

    @Override
    public void addCreativeSubtypes(String listType, List<FluidStack> list)
    {
        FluidStack defaultStack = new FluidStack(this, 1);

        if ((DrinkableFluid.SUBTYPE.equals(listType) || FluidFermentable.SUBTYPE_CLOSED.equals(listType)) && canDrink(defaultStack, null))
            list.add(new FluidStack(this, 1));

        if (ConsumableFluid.SUBTYPE.equals(listType) && canInject(defaultStack, null))
            list.add(new FluidStack(this, 1));
    }

    @Override
    public boolean canDrink(FluidStack fluidStack, EntityLivingBase entity)
    {
        boolean foodLevelOkay = !(entity instanceof EntityPlayer) || getFoodLevel(fluidStack) == null || ((EntityPlayer) entity).getFoodStats().needFood();
        return isDrinkable() && foodLevelOkay;
    }

    @Override
    public void drink(FluidStack fluidStack, EntityLivingBase entity)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(entity);

        if (drugProperties != null)
        {
            List<DrugInfluence> drugInfluences = new ArrayList<>();
            getDrugInfluences(fluidStack, drugInfluences);

            for (DrugInfluence influence : drugInfluences)
                drugProperties.addToDrug(influence);
        }

        if (foodLevel != null && entity instanceof EntityPlayer)
            ((EntityPlayer) entity).getFoodStats().addStats(foodLevel.getLeft(), foodLevel.getRight());
    }

    @Override
    public boolean canInject(FluidStack fluidStack, EntityLivingBase entity)
    {
        return isInjectable();
    }

    @Override
    public void inject(FluidStack fluidStack, EntityLivingBase entity)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(entity);

        if (drugProperties != null)
        {
            List<DrugInfluence> drugInfluences = new ArrayList<>();
            getDrugInfluences(fluidStack, drugInfluences);

            for (DrugInfluence influence : drugInfluences)
                drugProperties.addToDrug(influence);
        }
    }

    @Override
    public float fireStrength(FluidStack fluidStack)
    {
        return getAlcohol(fluidStack) * fluidStack.amount / FluidHelper.MILLIBUCKETS_PER_LITER * 2.0f;
    }

    @Override
    public float explosionStrength(FluidStack fluidStack)
    {
        return getAlcohol(fluidStack) * fluidStack.amount / FluidHelper.MILLIBUCKETS_PER_LITER * 0.6f;
    }

    public float getAlcohol(FluidStack fluidStack)
    {
        float alcohol = 0.0f;

        List<DrugInfluence> drugInfluences = new ArrayList<>();
        getDrugInfluences(fluidStack, drugInfluences);

        for (DrugInfluence drugInfluence : drugInfluences)
        {
            if (drugInfluence.getDrugName().equals("Alcohol"))
                alcohol += drugInfluence.getMaxInfluence();
        }
        return MathHelper.clamp_float(alcohol, 0.0f, 1.0f);
    }
}
