/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class SimpleDrug implements Drug {
    protected double effect;
    protected double effectActive;
    protected boolean locked = false;

    private final double decreaseSpeed;
    private final double decreaseSpeedPlus;
    private final boolean invisible;

    private final DrugType type;

    public SimpleDrug(DrugType type, double decSpeed, double decSpeedPlus) {
        this(type, decSpeed, decSpeedPlus, false);
    }

    public SimpleDrug(DrugType type, double decSpeed, double decSpeedPlus, boolean invisible) {
        this.type = type;
        decreaseSpeed = decSpeed;
        decreaseSpeedPlus = decSpeedPlus;

        this.invisible = invisible;
    }

    @Override
    public final DrugType getType() {
        return type;
    }

    public void setActiveValue(double value) {
        effectActive = value;
    }

    @Override
    public double getActiveValue() {
        return effectActive;
    }

    public double getDesiredValue() {
        return effect;
    }

    @Override
    public void setDesiredValue(double value) {
        effect = value;
    }

    @Override
    public void addToDesiredValue(double value) {
        if (!locked) {
            effect += value;
        }
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean isVisible() {
        return !invisible;
    }

    @Override
    public void update(DrugProperties drugProperties) {
        if (!locked) {
            effect *= decreaseSpeed;
            effect -= decreaseSpeedPlus;
        }

        effect = MathHelper.clamp(effect, 0, 1);
        effectActive = MathUtils.nearValue(effectActive, effect, 0.05, 0.005);
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        reset(drugProperties);
    }

    @Override
    public void reset(DrugProperties drugProperties) {
        if (!locked) {
            effect = 0;
        }
    }

    @Override
    public void fromNbt(NbtCompound par1NBTTagCompound) {
        setDesiredValue(par1NBTTagCompound.getDouble("effect"));
        setActiveValue(par1NBTTagCompound.getDouble("effectActive"));
        setLocked(par1NBTTagCompound.getBoolean("locked"));
    }

    @Override
    public void toNbt(NbtCompound compound) {
        compound.putDouble("effect", getDesiredValue());
        compound.putDouble("effectActive", getActiveValue());
        compound.putBoolean("locked", isLocked());
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public void applyContrastColorization(float[] rgba) {

    }

    @Override
    public void applyColorBloom(float[] rgba) {

    }
}
