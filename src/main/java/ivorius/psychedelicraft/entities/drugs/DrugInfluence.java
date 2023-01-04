/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.nbt.NbtCompound;

public class DrugInfluence implements NbtSerialisable {
    protected String drugName;

    protected int delay;

    protected double influenceSpeed;
    protected double influenceSpeedPlus;

    protected double maxInfluence;

    public DrugInfluence(String drugName, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        this.drugName = drugName;

        this.delay = delay;

        this.influenceSpeed = influenceSpeed;
        this.influenceSpeedPlus = influenceSpeedPlus;

        this.maxInfluence = maxInfluence;
    }

    public DrugInfluence() {

    }

    public String getDrugName() {
        return drugName;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public double getInfluenceSpeed() {
        return influenceSpeed;
    }

    public void setInfluenceSpeed(double influenceSpeed) {
        this.influenceSpeed = influenceSpeed;
    }

    public double getInfluenceSpeedPlus() {
        return influenceSpeedPlus;
    }

    public void setInfluenceSpeedPlus(double influenceSpeedPlus) {
        this.influenceSpeedPlus = influenceSpeedPlus;
    }

    public double getMaxInfluence() {
        return maxInfluence;
    }

    public void setMaxInfluence(double maxInfluence) {
        this.maxInfluence = maxInfluence;
    }

    public void update(DrugProperties drugProperties) {
        if (delay > 0) {
            delay--;
        }

        if (delay == 0 && maxInfluence > 0) {
            double addition = Math.min(maxInfluence, influenceSpeedPlus + maxInfluence * influenceSpeed);

            addToDrug(drugProperties, addition);
            maxInfluence -= addition;
        }
    }

    public void addToDrug(DrugProperties drugProperties, double value) {
        drugProperties.addToDrug(drugName, value);
    }

    public boolean isDone() {
        return maxInfluence <= 0.0;
    }

    @Override
    public DrugInfluence clone() {
        // TODO: (Sollace) Modernize
        DrugInfluence inf = null;
        try {
            inf = getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Psychedelicraft.logger.error("Exception whilst cloning DrugInfluence", e);
            e.printStackTrace();
        }

        if (inf != null) {
            inf.fromNbt(toNbt());
        }

        return inf;
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        drugName = compound.getString("drugName");
        delay = compound.getInt("delay");
        influenceSpeed = compound.getDouble("influenceSpeed");
        influenceSpeedPlus = compound.getDouble("influenceSpeedPlus");
        maxInfluence = compound.getDouble("maxInfluence");
    }

    @Override
    public void toNbt(NbtCompound compound) {
        compound.putString("drugName", drugName);
        compound.putInt("delay", delay);
        compound.putDouble("influenceSpeed", influenceSpeed);
        compound.putDouble("influenceSpeedPlus", influenceSpeedPlus);
        compound.putDouble("maxInfluence", maxInfluence);
    }
}
