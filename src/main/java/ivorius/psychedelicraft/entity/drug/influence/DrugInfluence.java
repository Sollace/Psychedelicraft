/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.influence;

import java.util.Locale;
import java.util.Optional;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class DrugInfluence implements NbtSerialisable {

    public static Optional<DrugInfluence> loadFromNbt(NbtCompound compound) {
        return InfluenceType.of(compound.getString("type")).map(type -> type.create(compound));
    }

    protected DrugType drugType;

    protected int delay;

    protected double influenceSpeed;
    protected double influenceSpeedPlus;

    protected double maxInfluence;

    private final InfluenceType type;

    public DrugInfluence(DrugType drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        this(InfluenceType.DEFAULT, drugType, delay, influenceSpeed, influenceSpeedPlus, maxInfluence);
    }

    public DrugInfluence(InfluenceType type, DrugType drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        this(type);
        this.drugType = drugType;

        this.delay = delay;

        this.influenceSpeed = influenceSpeed;
        this.influenceSpeedPlus = influenceSpeedPlus;

        this.maxInfluence = maxInfluence;
    }

    protected DrugInfluence(InfluenceType type) {
        this.type = type;
    }

    public final DrugType getDrugType() {
        return drugType;
    }

    public boolean isOf(DrugType type) {
        return getDrugType() == type;
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

    public boolean update(DrugProperties drugProperties) {
        if (delay > 0) {
            delay--;
        }

        if (delay == 0 && maxInfluence > 0) {
            double addition = Math.min(maxInfluence, influenceSpeedPlus + maxInfluence * influenceSpeed);

            addToDrug(drugProperties, addition);
            maxInfluence -= addition;
        }

        return isDone();
    }

    public void addToDrug(DrugProperties drugProperties, double value) {
        drugProperties.addToDrug(drugType, value);
    }

    public boolean isDone() {
        return maxInfluence <= 0.0;
    }

    @Override
    public DrugInfluence clone() {
        return type.create(toNbt());
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        if (compound.contains("drugName", NbtElement.STRING_TYPE)) {
            drugType = DrugType.REGISTRY.get(Psychedelicraft.id(compound.getString("drugName").toLowerCase(Locale.ROOT)));
        } else {
            drugType = DrugType.REGISTRY.get(Identifier.tryParse(compound.getString("drugType")));
        }
        delay = compound.getInt("delay");
        influenceSpeed = compound.getDouble("influenceSpeed");
        influenceSpeedPlus = compound.getDouble("influenceSpeedPlus");
        maxInfluence = compound.getDouble("maxInfluence");
    }

    @Override
    public void toNbt(NbtCompound compound) {
        compound.putString("type", type.identifier());
        compound.putString("drugType", drugType.id().toString());
        compound.putInt("delay", delay);
        compound.putDouble("influenceSpeed", influenceSpeed);
        compound.putDouble("influenceSpeedPlus", influenceSpeedPlus);
        compound.putDouble("maxInfluence", maxInfluence);
    }
}
