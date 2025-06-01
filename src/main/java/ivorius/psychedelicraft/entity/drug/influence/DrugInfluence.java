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
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.minecraft.network.PacketByteBuf;

public class DrugInfluence {
    public static final Codec<Vector3f> COLOR_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("r").forGetter(Vector3f::x),
            Codec.FLOAT.fieldOf("g").forGetter(Vector3f::y),
            Codec.FLOAT.fieldOf("b").forGetter(Vector3f::z)
    ).apply(i, Vector3f::new));
    public static final PacketCodec<PacketByteBuf, Optional<Vector3f>> COLOR_PACKET_CODEC = PacketCodecs.optional(PacketCodec.tuple(
        PacketCodecs.FLOAT, Vector3f::x,
        PacketCodecs.FLOAT, Vector3f::y,
        PacketCodecs.FLOAT, Vector3f::z,
        Vector3f::new
    ));
    public static final Codec<DrugInfluence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DrugType.REGISTRY.getCodec().fieldOf("drugType").forGetter(DrugInfluence::getDrugType),
            Codec.INT.fieldOf("delay").forGetter(DrugInfluence::getDelay),
            Codec.DOUBLE.fieldOf("influenceSpeed").forGetter(DrugInfluence::getInfluenceDelta),
            Codec.DOUBLE.fieldOf("influenceSpeedPlus").forGetter(DrugInfluence::getBaseIncrease),
            Codec.DOUBLE.fieldOf("maxInfluence").forGetter(DrugInfluence::getTargetInfluence),
            COLOR_CODEC.optionalFieldOf("color").forGetter(DrugInfluence::getColor)
    ).apply(instance, DrugInfluence::new));
    public static final Codec<List<DrugInfluence>> LIST_CODEC = CODEC.listOf();
    public static final PacketCodec<PacketByteBuf, DrugInfluence> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(DrugType.REGISTRY.getKey()), DrugInfluence::getDrugType,
            PacketCodecs.INTEGER, DrugInfluence::getDelay,
            PacketCodecs.DOUBLE, DrugInfluence::getInfluenceDelta,
            PacketCodecs.DOUBLE, DrugInfluence::getBaseIncrease,
            PacketCodecs.DOUBLE, DrugInfluence::getTargetInfluence,
            COLOR_PACKET_CODEC, DrugInfluence::getColor,
            DrugInfluence::new
    );

    protected DrugType<?> drugType;

    protected int delay;

    protected double influenceDelta;
    protected double baseIncrease;

    protected double targetInfluence;

    private final Optional<Vector3f> color;

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target) {
        this(drugType, delay, factor, base, target, Optional.empty());
    }

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, Vector3f color) {
        this(drugType, delay, factor, base, target, Optional.of(color));
    }

    private DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, Optional<Vector3f> color) {
        this.drugType = drugType;
        this.delay = delay;
        this.influenceDelta = factor;
        this.baseIncrease = base;
        this.targetInfluence = target;
        this.color = color;
    }

    public final DrugType<?> getDrugType() {
        return drugType;
    }

    public boolean isOf(DrugType<?> type) {
        return getDrugType() == type;
    }

    public int getDelay() {
        return delay;
    }

    public double getInfluenceDelta() {
        return influenceDelta;
    }

    public double getBaseIncrease() {
        return baseIncrease;
    }

    public double getTargetInfluence() {
        return targetInfluence;
    }

    public Optional<Vector3f> getColor() {
        return color;
    }

    public double getCurrentStrength() {
        return Math.min(targetInfluence, baseIncrease + targetInfluence * influenceDelta);
    }

    public boolean update(DrugProperties drugProperties) {
        if (delay > 0) {
            delay--;
        }

        if (delay == 0 && targetInfluence > 0) {
            double addition = getCurrentStrength();
            addToDrug(drugProperties, addition);
            targetInfluence -= addition;
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
        return targetInfluence <= 0.0;
    }

    public DrugInfluence copyWithMaximum(double maxInfluence) {
        return new DrugInfluence(drugType, delay, influenceDelta, baseIncrease, maxInfluence, color);
    }

    public DrugInfluence copyWithDelay(int delay) {
        return new DrugInfluence(drugType, delay, influenceDelta, baseIncrease, targetInfluence, color);
    }

    @Override
    public final DrugInfluence clone() {
        return copyWithMaximum(targetInfluence);
    }

    public interface DelayType {
        int IMMEDIATE = 0;
        int INGESTED = 15;
        int INHALED = 20;
        int CONTACT = 30;
        int METABOLISED = 60;
    }
}
