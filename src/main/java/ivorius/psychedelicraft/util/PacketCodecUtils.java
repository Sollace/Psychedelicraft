package ivorius.psychedelicraft.util;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.datafixers.util.Function7;

import io.netty.buffer.ByteBuf;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.intprovider.IntProvider;

public interface PacketCodecUtils {
    PacketCodec<ByteBuf, Optional<Integer>> OPTIONAL_INT = PacketCodecs.optional(PacketCodecs.INTEGER);
    PacketCodec<ByteBuf, Optional<Long>> OPTIONAL_VAR_LONG = PacketCodecs.optional(PacketCodecs.VAR_LONG);
    PacketCodec<ByteBuf, IntRange> INT_RANGE = PacketCodec.ofStatic((buffer, range) -> {
        OPTIONAL_INT.encode(buffer, Optional.ofNullable(range.getMin()));
        OPTIONAL_INT.encode(buffer, Optional.ofNullable(range.getMax()));
    }, buffer -> {
        Optional<Integer> min = OPTIONAL_INT.decode(buffer);
        Optional<Integer> max = OPTIONAL_INT.decode(buffer);
        if (min.isPresent() && max.isPresent()) {
            return IntRange.between(min.get(), max.get());
        }
        if (min.isPresent()) {
            return IntRange.atLeast(min.get());
        }
        if (max.isPresent()) {
            return IntRange.atMost(max.get());
        }
        return IntRange.ANY;
    });
    PacketCodec<PacketByteBuf, IntProvider> INT_PROVIDER_VALUE_CODEC = PacketCodecs.optional(PacketCodecs.NBT_ELEMENT).xmap(
            nbt -> nbt.flatMap(i -> IntProvider.VALUE_CODEC.decode(NbtOps.INSTANCE, i).result().map(pair -> pair.getFirst())).orElseThrow(),
            input -> IntProvider.VALUE_CODEC.encodeStart(NbtOps.INSTANCE, input).result());

    static <T extends Enum<T>> PacketCodec<PacketByteBuf, T> ofEnum(Class<T> type) {
        return PacketCodec.ofStatic(PacketByteBuf::writeEnumConstant, b -> b.readEnumConstant(type));
    }

    static<B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, DefaultedList<V>> toDefaultedList() {
        return PacketCodecs.toCollection(DefaultedList::ofSize);
    }

    static <B extends ByteBuf, C, T1, T2, T3, T4, T5, T6, T7> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b), codec4.decode(b), codec5.decode(b), codec6.decode(b), codec7.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                    codec4.encode(b, from4.apply(c));
                    codec5.encode(b, from5.apply(c));
                    codec6.encode(b, from6.apply(c));
                    codec7.encode(b, from7.apply(c));
                }
            };
        }
}
