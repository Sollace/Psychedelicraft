/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSGameRules;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.random.Random;

/**
 * Created by Sollace on April 19 2023.
 */
public class SleepDeprivationDrug extends SimpleDrug {
    public static final int TICKS_PER_DAY = 24000;
    public static final int TICKS_UNTIL_PHANTOM_SPAWN = TICKS_PER_DAY * 3;

    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(MOVEMENT_HALLUCINATION_STRENGTH, f -> Math.max(0, f - 0.8F) * 3)
            .put(CONTEXTUAL_HALLUCINATION_STRENGTH, f -> Math.max(0, f - 0.8F) * 4)
            .put(SOUND_VOLUME, f -> 1 + Math.max(0, (f - 0.5F) * 2))
            .put(DESATURATION_HALLUCINATION_STRENGTH, f -> Math.max(0, (f - 0.5F) * 2))
            .put(DROWSYNESS, 1)
            .put(MOTION_BLUR, f -> Math.max(0, f - 0.6F) * 3)
            .put(DIG_SPEED, f -> 1 - Math.max(0, f * 0.9F - 0.4F))
            .put(SPEED, f -> 1 - Math.max(0, f * 0.9F - 0.4F))
            .build();

    private static final float INCREASE_PER_TICKS = 1F / TICKS_UNTIL_PHANTOM_SPAWN;

    private float storedEnergy;

    public SleepDeprivationDrug() {
        super(DrugType.SLEEP_DEPRIVATION, 1, 0);
    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, Random random) {
        float caffiene = properties.getDrugValue(DrugType.CAFFEINE) + properties.getDrugValue(DrugType.COCAINE) * 3;

        storedEnergy = MathUtils.approach(storedEnergy, Math.min(1, caffiene * 10F), 0.02F);

        if (caffiene > 0.1F) {
            setDesiredValue(0);
        } else {
            if (properties.asEntity().getWorld().getGameRules().getBoolean(PSGameRules.DO_SLEEP_DEPRIVATION)) {
                setDesiredValue(getDesiredValue() + (INCREASE_PER_TICKS / 3));
            }
        }

        return super.tickSideEffects(properties, random);
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
    public void reset(DrugProperties drugProperties) {
        super.reset(drugProperties);
        if (!locked) {
            storedEnergy = 0;
        }
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        super.fromNbt(compound, lookup);
        storedEnergy = compound.getFloat("storedEnergy");
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        super.toNbt(compound, lookup);
        compound.putFloat("storedEnergy", storedEnergy);
    }
}
