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
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public class DrugInfluenceInstance {
    public static final Codec<DrugInfluenceInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DrugType.REGISTRY.getCodec().fieldOf("drugType").forGetter(i -> i.drugType),
            Codec.INT.fieldOf("delay").forGetter(i -> i.delay),
            Codec.DOUBLE.fieldOf("influenceSpeed").forGetter(i -> i.factor),
            Codec.DOUBLE.fieldOf("influenceSpeedPlus").forGetter(i -> i.base),
            Codec.DOUBLE.fieldOf("maxInfluence").forGetter(i -> i.targetRemaining),
            DrugInfluence.COLOR_CODEC.optionalFieldOf("color").forGetter(i -> i.color)
    ).apply(instance, DrugInfluenceInstance::new));
    public static final Codec<List<DrugInfluenceInstance>> LIST_CODEC = CODEC.listOf();
    public static final PacketCodec<RegistryByteBuf, DrugInfluenceInstance> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(DrugType.REGISTRY.getKey()), i -> i.drugType,
            PacketCodecs.INTEGER, i -> i.delay,
            PacketCodecs.DOUBLE, i -> i.factor,
            PacketCodecs.DOUBLE, i -> i.base,
            PacketCodecs.DOUBLE, i -> i.targetRemaining,
            DrugInfluence.COLOR_PACKET_CODEC, i -> i.color,
            DrugInfluenceInstance::new
    );

    public final DrugType<?> drugType;
    public final double factor;
    public final double base;
    public final Optional<Vector3f> color;

    private int delay;
    private double targetRemaining;

    public DrugInfluenceInstance(DrugInfluence influence) {
        this(influence.drugType(), influence.delay(), influence.factor(), influence.base(), influence.target(), influence.color());
    }

    public DrugInfluenceInstance(DrugType<?> drugType, int delay, double factor, double base, double target, Optional<Vector3f> color) {
        this.drugType = drugType;
        this.delay = delay;
        this.factor = factor;
        this.base = base;
        this.targetRemaining = target;
        this.color = color;
    }

    public boolean isOf(DrugType<?> type) {
        return drugType == type;
    }

    public boolean update(DrugProperties properties) {
        if (--delay > 0) {
            return false;
        }

        if (targetRemaining > 0) {
            double addition = Math.min(targetRemaining, base + targetRemaining * factor);
            properties.addToDrug(drugType, addition, this);
            targetRemaining -= addition;
        }

        return targetRemaining <= 0;
    }
}
