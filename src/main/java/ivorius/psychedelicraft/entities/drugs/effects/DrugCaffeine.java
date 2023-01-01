/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugCaffeine extends DrugSimple {
    public DrugCaffeine(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float heartbeatVolume() {
        return MathHelper.lerp((float) getActiveValue(), 0.6F, 1.0F);
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.2f;
    }

    @Override
    public float breathVolume() {
        return MathHelper.lerp((float) getActiveValue(), 0.4F, 1) * 0.5f;
    }

    @Override
    public float breathSpeed() {
        return (float) getActiveValue() * 0.3f;
    }

    @Override
    public float randomJumpChance() {
        return MathHelper.lerp((float) getActiveValue(), 0.6F, 1) * 0.07f;
    }

    @Override
    public float randomPunchChance() {
        return MathHelper.lerp((float) getActiveValue(), 0.3F, 1) * 0.05f;
    }

    @Override
    public float speedModifier() {
        return 1 + (float) getActiveValue() * 0.2F;
    }

    @Override
    public float digSpeedModifier() {
        return 1 + (float) getActiveValue() * 0.2F;
    }

    @Override
    public EntityPlayer.EnumStatus getSleepStatus() {
        return getActiveValue() > 0.1 ? Psychedelicraft.sleepStatusDrugs : null;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.3f;
    }

    @Override
    public float handTrembleStrength() {
        return MathHelper.lerp((float) getActiveValue(), 0.6F, 1.0F);
    }

    @Override
    public float viewTrembleStrength()
    {
        return MathHelper.lerp((float) getActiveValue(), 0.8F, 1.0F);
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.lerp((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.03F;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.lerp((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.03F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.lerp((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.05F;
    }
}
