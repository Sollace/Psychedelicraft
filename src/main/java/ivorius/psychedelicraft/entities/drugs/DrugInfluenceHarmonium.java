/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.psychedelicraft.entities.drugs.effects.DrugHarmonium;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 10.03.14.
 */
public class DrugInfluenceHarmonium extends DrugInfluence {
    public float[] color;

    public DrugInfluenceHarmonium(String drugName, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, float[] color) {
        super(drugName, delay, influenceSpeed, influenceSpeedPlus, maxInfluence);

        this.color = color;
    }

    public DrugInfluenceHarmonium() {
        super();
        color = new float[3];
    }

    @Override
    public void addToDrug(DrugProperties drugProperties, double value)
    {
        super.addToDrug(drugProperties, value);

        Drug drug = drugProperties.getDrug(getDrugName());

        if (drug instanceof DrugHarmonium) {
            DrugHarmonium harmonium = (DrugHarmonium) drug;

            double inf = value + (1.0f - value) * (1.0f - harmonium.getActiveValue());
            harmonium.currentColor[0] = (float) IvMathHelper.mix(harmonium.currentColor[0], color[0], inf);
            harmonium.currentColor[1] = (float) IvMathHelper.mix(harmonium.currentColor[1], color[1], inf);
            harmonium.currentColor[2] = (float) IvMathHelper.mix(harmonium.currentColor[2], color[2], inf);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setFloat("color[0]", color[0]);
        compound.setFloat("color[1]", color[1]);
        compound.setFloat("color[2]", color[2]);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        color[0] = compound.getFloat("color[0]");
        color[1] = compound.getFloat("color[1]");
        color[2] = compound.getFloat("color[2]");
    }
}
