/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugCannabis extends DrugSimple
{
    public DrugCannabis(double decSpeed, double decSpeedPlus)
    {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public void update(LivingEntity entity, DrugProperties drugProperties)
    {
        super.update(entity, drugProperties);

        if (getActiveValue() > 0.0)
        {
            if (entity instanceof PlayerEntity player)
                player.addExhaustion(0.03F * (float) getActiveValue());
        }
    }

    @Override
    public float speedModifier()
    {
        return (1.0F - (float) getActiveValue()) * 0.5F + 0.5F;
    }

    @Override
    public float digSpeedModifier()
    {
        return (1.0F - (float) getActiveValue()) * 0.5F + 0.5F;
    }

    @Override
    public float superSaturationHallucinationStrength()
    {
        return MathHelper.lerp((float)getActiveValue(), 0.0f, 0.5f) * 0.3f;
    }

    @Override
    public float colorHallucinationStrength()
    {
        return MathHelper.lerp((float) getActiveValue() * 1.3f, 0.5f, 1.0f) * 0.1f;
    }

    @Override
    public float movementHallucinationStrength()
    {
        return MathHelper.lerp((float) getActiveValue() * 1.3f, 0.5f, 1.0f) * 0.1f;
    }

    @Override
    public float contextualHallucinationStrength()
    {
        return MathHelper.lerp((float) getActiveValue() * 1.3f, 0.5f, 1.0f) * 0.1f;
    }

    @Override
    public float headMotionInertness()
    {
        return (float)getActiveValue() * 8.0f;
    }

    @Override
    public float viewWobblyness()
    {
        return (float)getActiveValue() * 0.02f;
    }
}
