/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.influence;

import java.util.List;
import java.util.Optional;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.type.HarmoniumDrug;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public class DrugInfluence {
    private static final Codec<Vector3f> COLOR_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("r").forGetter(Vector3f::x),
            Codec.FLOAT.fieldOf("g").forGetter(Vector3f::y),
            Codec.FLOAT.fieldOf("b").forGetter(Vector3f::z)
    ).apply(i, Vector3f::new));
    private static final PacketCodec<RegistryByteBuf, Optional<Vector3f>> COLOR_PACKET_CODEC = PacketCodecs.optional(PacketCodec.tuple(
        PacketCodecs.FLOAT, Vector3f::x,
        PacketCodecs.FLOAT, Vector3f::y,
        PacketCodecs.FLOAT, Vector3f::z,
        Vector3f::new
    ));
    public static final Codec<DrugInfluence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DrugType.REGISTRY.getCodec().fieldOf("drugType").forGetter(DrugInfluence::getDrugType),
            Codec.INT.fieldOf("delay").forGetter(DrugInfluence::getDelay),
            Codec.DOUBLE.fieldOf("influenceSpeed").forGetter(DrugInfluence::getInfluenceSpeed),
            Codec.DOUBLE.fieldOf("influenceSpeedPlus").forGetter(DrugInfluence::getInfluenceSpeedPlus),
            Codec.DOUBLE.fieldOf("maxInfluence").forGetter(DrugInfluence::getMaxInfluence),
            COLOR_CODEC.optionalFieldOf("color").forGetter(DrugInfluence::getColor)
    ).apply(instance, DrugInfluence::new));
    public static final Codec<List<DrugInfluence>> LIST_CODEC = CODEC.listOf();
    public static final PacketCodec<RegistryByteBuf, DrugInfluence> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(DrugType.REGISTRY.getKey()), DrugInfluence::getDrugType,
            PacketCodecs.INTEGER, DrugInfluence::getDelay,
            PacketCodecs.DOUBLE, DrugInfluence::getInfluenceSpeed,
            PacketCodecs.DOUBLE, DrugInfluence::getInfluenceSpeedPlus,
            PacketCodecs.DOUBLE, DrugInfluence::getMaxInfluence,
            COLOR_PACKET_CODEC, DrugInfluence::getColor,
            DrugInfluence::new
    );

    protected DrugType drugType;

    protected int delay;

    protected double influenceSpeed;
    protected double influenceSpeedPlus;

    protected double maxInfluence;

    private final Optional<Vector3f> color;

    public DrugInfluence(DrugType drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        this(drugType, delay, influenceSpeed, influenceSpeedPlus, maxInfluence, Optional.empty());
    }

    public DrugInfluence(DrugType drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, Vector3f color) {
        this(drugType, delay, influenceSpeed, influenceSpeedPlus, maxInfluence, Optional.of(color));
    }

    private DrugInfluence(DrugType drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, Optional<Vector3f> color) {
        this.drugType = drugType;
        this.delay = delay;
        this.influenceSpeed = influenceSpeed;
        this.influenceSpeedPlus = influenceSpeedPlus;
        this.maxInfluence = maxInfluence;
        this.color = color;
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

    public double getInfluenceSpeed() {
        return influenceSpeed;
    }

    public double getInfluenceSpeedPlus() {
        return influenceSpeedPlus;
    }

    public double getMaxInfluence() {
        return maxInfluence;
    }

    public Optional<Vector3f> getColor() {
        return color;
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
        color.ifPresent(color -> {
            if (drugProperties.getDrug(getDrugType()) instanceof HarmoniumDrug harmonium) {
                MathUtils.lerp((float)(value + (1 - value) * (1 - harmonium.getActiveValue())), harmonium.currentColor, color);
            }
        });
    }

    public boolean isDone() {
        return maxInfluence <= 0.0;
    }

    public DrugInfluence copyWithMaximum(double maxInfluence) {
        return new DrugInfluence(drugType, delay, influenceSpeed, influenceSpeedPlus, maxInfluence, color);
    }

    @Override
    public final DrugInfluence clone() {
        return copyWithMaximum(maxInfluence);
    }

    public interface DelayType {
        int IMMEDIATE = 0;
        int INGESTED = 15;
        int INHALED = 20;
        int CONTACT = 30;
        int METABOLISED = 60;
    }
}
