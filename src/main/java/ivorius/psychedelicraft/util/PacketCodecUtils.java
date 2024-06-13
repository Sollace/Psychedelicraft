package ivorius.psychedelicraft.util;

import java.util.function.Function;

import com.mojang.datafixers.util.Function7;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.collection.DefaultedList;

public interface PacketCodecUtils {
    static <T extends Enum<T>> PacketCodec<RegistryByteBuf, T> ofEnum(Class<T> type) {
        return PacketCodec.ofStatic(RegistryByteBuf::writeEnumConstant, b -> b.readEnumConstant(type));
    }

    static<B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, DefaultedList<V>> toDefaultedList(V empty) {
        return PacketCodecs.toCollection(size -> DefaultedList.ofSize(size, empty));
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    T1 object2 = codec1.decode(b);
                    T2 object3 = codec2.decode(b);
                    T3 object4 = codec3.decode(b);
                    T4 object5 = codec4.decode(b);
                    T5 object6 = codec5.decode(b);
                    T6 object7 = codec6.decode(b);
                    T7 object8 = codec7.decode(b);
                    return to.apply(object2, object3, object4, object5, object6, object7, object8);
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
