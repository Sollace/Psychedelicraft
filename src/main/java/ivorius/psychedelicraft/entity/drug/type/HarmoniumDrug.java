/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;

public class HarmoniumDrug extends SimpleDrug {
    public float[] currentColor = new float[]{1.0f, 1.0f, 1.0f};

    public HarmoniumDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.HARMONIUM, decSpeed, decSpeedPlus);
    }

    @Override
    public void applyContrastColorization(float[] rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue());
    }

    @Override
    public void applyColorBloom(float[] rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue() * 3);
    }

    @Override
    public void toNbt(NbtCompound tagCompound) {
        super.toNbt(tagCompound);
        tagCompound.putFloat("currentColor[0]", currentColor[0]);
        tagCompound.putFloat("currentColor[1]", currentColor[1]);
        tagCompound.putFloat("currentColor[2]", currentColor[2]);
    }

    @Override
    public void fromNbt(NbtCompound tagCompound) {
        super.fromNbt(tagCompound);
        currentColor[0] = tagCompound.getFloat("currentColor[0]");
        currentColor[1] = tagCompound.getFloat("currentColor[1]");
        currentColor[2] = tagCompound.getFloat("currentColor[2]");
    }
}
