/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSGameRules;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;

/**
 * Created by Sollace on April 19 2023.
 */
public class SleepDeprivationDrug extends SimpleDrug {
    public static final int TICKS_PER_DAY = 24000;
    public static final int TICKS_UNTIL_PHANTOM_SPAWN = TICKS_PER_DAY * 3;

    private static final float INCREASE_PER_TICKS = 1F / TICKS_UNTIL_PHANTOM_SPAWN;

    private float storedEnergy;

    public SleepDeprivationDrug() {
        super(DrugType.SLEEP_DEPRIVATION, 1, 0);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        float caffiene = drugProperties.getDrugValue(DrugType.CAFFEINE) + drugProperties.getDrugValue(DrugType.COCAINE);

        storedEnergy = MathUtils.approach(storedEnergy, Math.min(1, caffiene * 10F), 0.02F);

        if (caffiene > 0.1F) {
            setDesiredValue(0);
        } else {
            if (drugProperties.asEntity().getWorld().getGameRules().getBoolean(PSGameRules.DO_SLEEP_DEPRIVATION)) {
                setDesiredValue(getDesiredValue() + (INCREASE_PER_TICKS / 3));
            }
        }
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        super.onWakeUp(drugProperties);
        setActiveValue(0);
    }

    @Override
    public double getActiveValue() {
        return Math.max(0, super.getActiveValue() - storedEnergy);
    }

    @Override
    public float digSpeedModifier() {
        return 1 - Math.max(0, (float)getActiveValue() * 0.9F - 0.4F);
    }

    @Override
    public float speedModifier() {
        return digSpeedModifier();
    }

    @Override
    public float motionBlur() {
        return Math.max(0, (float)getActiveValue() - 0.6F) * 3;
    }

    @Override
    public float drowsyness() {
        return (float)getActiveValue();
    }

    @Override
    public float headMotionInertness() {
        return 0;
    }

    @Override
    public float desaturationHallucinationStrength() {
        return Math.max(0, ((float)getActiveValue() - 0.5F) * 2);
    }

    @Override
    public float soundVolumeModifier() {
        return 1 + desaturationHallucinationStrength();
    }

    @Override
    public float contextualHallucinationStrength() {
        return Math.max(0, (float)getActiveValue() - 0.8F) * 4;
    }

    @Override
    public float movementHallucinationStrength() {
        return Math.max(0, (float)getActiveValue() - 0.8F) * 3;
    }

    @Override
    public void reset(DrugProperties drugProperties) {
        super.reset(drugProperties);
        if (!locked) {
            storedEnergy = 0;
        }
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        super.fromNbt(compound);
        storedEnergy = compound.getFloat("storedEnergy");
    }

    @Override
    public void toNbt(NbtCompound compound) {
        super.toNbt(compound);
        compound.putFloat("storedEnergy", storedEnergy);
    }

}
