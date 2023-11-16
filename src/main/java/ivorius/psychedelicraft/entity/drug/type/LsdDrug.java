/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Sollace on Feb 6 2023.
 */
public class LsdDrug extends SimpleDrug {

    private final boolean harmful;

    public LsdDrug(DrugType type, double decSpeed, double decSpeedPlus, boolean harmful) {
        super(type, decSpeed, decSpeedPlus);
        this.harmful = harmful;
    }

    @Override
    public void update(DrugProperties drugProperties) {
        if (harmful && getActiveValue() >= 0.99F) {
            Drug caffiene = drugProperties.getDrug(DrugType.CAFFEINE);
            if (caffiene.getActiveValue() > 0) {
                caffiene.addToDesiredValue(-0.5);
                effect /= 2;
            } else {
                drugProperties.asEntity().damage(drugProperties.damageOf(PSDamageTypes.STROKE), 1);
            }
        }
        super.update(drugProperties);
    }

    @Override
    public float handTrembleStrength() {
        return MathUtils.inverseLerp((float) getActiveValue(), 0.6F, 1);
    }

    @Override
    public float viewTrembleStrength() {
        return MathUtils.inverseLerp((float) getActiveValue(), 0.8F, 1);
    }

    @Override
    public float speedModifier() {
        return 1 + (float) getActiveValue() * 0.1F;
    }

    @Override
    public float digSpeedModifier() {
        return 1 + (float) getActiveValue() * 0.1F;
    }

    @Override
    public float soundVolumeModifier() {
        float strength = (MathHelper.clamp(getTicksActive(), 50, 250) - 50) / 200F;
        return 1 + (float)getActiveValue() * 1.75f * strength;
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) Math.max(0, getActiveValue() - 0.6F) * 2.8F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) Math.max(0, getActiveValue() - 0.6F) * 1.9F;
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float) getActiveValue() * 0.12F;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.8F;
    }

    @Override
    public float hungerSuppression() {
        return (float)getActiveValue() * 0.2F;
    }

    @Override
    public float weightlessness() {
        if (getActiveValue() > 0.6F) {
            return (float) getActiveValue() * 0.8F;
        }
        return (float) getActiveValue() * 0.2F;
    }
}
