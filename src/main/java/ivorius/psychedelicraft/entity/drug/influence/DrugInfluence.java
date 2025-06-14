/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.influence;

import java.util.OptionalInt;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, OptionalInt color) {
    public static final Codec<Integer> COLOR_CODEC = RecordCodecBuilder.<Vector3f>create(i -> i.group(
            Codec.FLOAT.fieldOf("r").forGetter(Vector3f::x),
            Codec.FLOAT.fieldOf("g").forGetter(Vector3f::y),
            Codec.FLOAT.fieldOf("b").forGetter(Vector3f::z)
    ).apply(i, Vector3f::new)).xmap(MathUtils::getArgb, MathUtils::unpackRgb);
    public static final Codec<DrugInfluence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DrugType.REGISTRY.getCodec().fieldOf("drugType").forGetter(DrugInfluence::drugType),
            Codec.INT.fieldOf("delay").forGetter(DrugInfluence::delay),
            Codec.DOUBLE.fieldOf("influenceSpeed").forGetter(DrugInfluence::factor),
            Codec.DOUBLE.fieldOf("influenceSpeedPlus").forGetter(DrugInfluence::base),
            Codec.DOUBLE.fieldOf("maxInfluence").forGetter(DrugInfluence::target),
            CodecUtils.optionalIntFieldOf(COLOR_CODEC, "color").forGetter(DrugInfluence::color)
    ).apply(instance, DrugInfluence::new));
    public static final PacketCodec<RegistryByteBuf, DrugInfluence> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(DrugType.REGISTRY.getKey()), DrugInfluence::drugType,
            PacketCodecs.INTEGER, DrugInfluence::delay,
            PacketCodecs.DOUBLE, DrugInfluence::factor,
            PacketCodecs.DOUBLE, DrugInfluence::base,
            PacketCodecs.DOUBLE, DrugInfluence::target,
            PacketCodecs.OPTIONAL_INT, DrugInfluence::color,
            DrugInfluence::new
    );

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target) {
        this(drugType, delay, factor, base, target, OptionalInt.empty());
    }

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, int color) {
        this(drugType, delay, factor, base, target, OptionalInt.of(color));
    }

    public boolean isOf(DrugType<?> type) {
        return drugType() == type;
    }

    public DrugInfluence copyWithTarget(double target) {
        return new DrugInfluence(drugType, delay, factor, base, target, color);
    }

    public DrugInfluence copyWithDelay(int delay) {
        return new DrugInfluence(drugType, delay, factor, base, target, color);
    }
}
