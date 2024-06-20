/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import org.joml.Vector3f;
import org.joml.Vector4f;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class HarmoniumDrug extends SimpleDrug {
    public final Vector3f currentColor = new Vector3f(1, 1, 1);

    public HarmoniumDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.HARMONIUM, decSpeed, decSpeedPlus);
    }

    @Override
    public void applyContrastColorization(Vector4f rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue(), false);
    }

    @Override
    public void applyColorBloom(Vector4f rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue() * 3, false);
    }

    @Override
    public void toNbt(NbtCompound tagCompound, WrapperLookup lookup) {
        super.toNbt(tagCompound, lookup);
        tagCompound.putFloat("currentColor[0]", currentColor.x);
        tagCompound.putFloat("currentColor[1]", currentColor.y);
        tagCompound.putFloat("currentColor[2]", currentColor.z);
    }

    @Override
    public void fromNbt(NbtCompound tagCompound, WrapperLookup lookup) {
        super.fromNbt(tagCompound, lookup);
        currentColor.set(
                tagCompound.getFloat("currentColor[0]"),
                tagCompound.getFloat("currentColor[1]"),
                tagCompound.getFloat("currentColor[2]")
        );
    }
}
