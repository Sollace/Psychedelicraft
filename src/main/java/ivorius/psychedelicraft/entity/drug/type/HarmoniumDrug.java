/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import org.joml.Vector3f;
import org.joml.Vector4f;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluenceInstance;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class HarmoniumDrug extends SimpleDrug {
    private final Vector3f currentColor = new Vector3f(1, 1, 1);

    public HarmoniumDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.HARMONIUM, decSpeed, decSpeedPlus);
    }

    @Override
    public void applyContrastColorization(Vector4f rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue());
    }

    @Override
    public void applyColorBloom(Vector4f rgba) {
        MathUtils.mixColorsDynamic(currentColor, rgba, (float) getActiveValue() * 3);
    }

    @Override
    public void addToDesiredValue(double value, DrugInfluenceInstance influence) {
        super.addToDesiredValue(value, influence);
        if (!isLocked()) {
            influence.color.ifPresent(color -> {
                MathUtils.lerp((float)(value + (1 - value) * (1 - getActiveValue())), currentColor, MathUtils.unpackRgb(color));
            });
        }

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
