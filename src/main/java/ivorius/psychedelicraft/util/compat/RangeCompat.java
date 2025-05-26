package ivorius.psychedelicraft.util.compat;

import com.mojang.serialization.Codec;

import net.minecraft.predicate.NumberRange.FloatRange;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.util.dynamic.Codecs;

public interface RangeCompat {
    Codec<IntRange> INT_CODEC = Codecs.JSON_ELEMENT.xmap(IntRange::fromJson, IntRange::toJson);
    Codec<FloatRange> FLOAT_CODEC = Codecs.JSON_ELEMENT.xmap(FloatRange::fromJson, FloatRange::toJson);
}
