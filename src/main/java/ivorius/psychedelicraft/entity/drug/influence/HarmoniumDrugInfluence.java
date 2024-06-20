/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.influence;

import org.joml.Vector3f;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.HarmoniumDrug;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

/**
 * Created by lukas on 10.03.14.
 */
public class HarmoniumDrugInfluence extends DrugInfluence {

    private final Vector3f color;

    public HarmoniumDrugInfluence(int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, Vector3f color) {
        super(InfluenceType.HARMONIUM, DrugType.HARMONIUM, delay, influenceSpeed, influenceSpeedPlus, maxInfluence);
        this.color = color;
    }

    public HarmoniumDrugInfluence(InfluenceType type) {
        super(type);
        color = new Vector3f(1, 1, 1);
    }

    @Override
    public void addToDrug(DrugProperties drugProperties, double value) {
        super.addToDrug(drugProperties, value);
        if (drugProperties.getDrug(getDrugType()) instanceof HarmoniumDrug harmonium) {
            MathUtils.lerp((float)(value + (1 - value) * (1 - harmonium.getActiveValue())), harmonium.currentColor, color);
        }
    }

    @Override
    protected void copyFrom(DrugInfluence old) {
        super.copyFrom(old);
        if (old instanceof HarmoniumDrugInfluence o) {
            color.set(o.color);
        }
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        super.fromNbt(compound, lookup);
        color.set(compound.getFloat("color[0]"), compound.getFloat("color[1]"), compound.getFloat("color[2]"));
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        super.toNbt(compound, lookup);
        compound.putFloat("color[0]", color.x);
        compound.putFloat("color[1]", color.y);
        compound.putFloat("color[2]", color.z);
    }
}
