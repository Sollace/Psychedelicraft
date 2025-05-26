package ivorius.psychedelicraft.util.compat;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;

import io.netty.buffer.ByteBuf;

public interface PacketCodec<Buff extends ByteBuf, T> {

    void encode(Buff buffer, T value);

    T decode(Buff buffer);

    default <X> PacketCodec<Buff, X> xmap(Function<T, X> to, Function<X, T> from) {
        var self = this;
        return PacketCodec.ofStatic(
                (buffer, value) -> self.encode(buffer, from.apply(value)),
                buffer -> to.apply(self.decode(buffer))
        );
    }

    default <C extends Collection<T>> PacketCodec<Buff, C> collect(ResultFunction<Buff, T, C> collector) {
        var self = this;
        return PacketCodec.ofStatic(
                (buffer, value) -> collector.encode(buffer, value, self),
                buffer -> collector.decode(buffer, self)
        );
    }

    interface ResultFunction<Buff extends ByteBuf, T, Result> {
        void encode(Buff buffer, Result value, PacketCodec<? super Buff, T> elementCodec);

        Result decode(Buff buffer, PacketCodec<? super Buff, T> elementCodec);
    }

    static <Buff extends ByteBuf, T> PacketCodec<Buff, T> ofStatic(BiConsumer<Buff, T> encoder, Function<Buff, T> decoder) {
        return new PacketCodec<>() {
            @Override
            public void encode(Buff buffer, T value) {
                encoder.accept(buffer, value);
            }

            @Override
            public T decode(Buff buffer) {
                return decoder.apply(buffer);
            }
        };
    }

    static <B extends ByteBuf, C, T1> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            Function<T1, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                }
            };
        }

    static <B extends ByteBuf, C, T1, T2> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            BiFunction<T1, T2, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                }
            };
        }

    static <B extends ByteBuf, C, T1, T2, T3> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            Function3<T1, T2, T3, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                }
            };
        }

    static <B extends ByteBuf, C, T1, T2, T3, T4> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            Function4<T1, T2, T3, T4, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b), codec4.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                    codec4.encode(b, from4.apply(c));
                }
            };
        }

    static <B extends ByteBuf, C, T1, T2, T3, T4, T5> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            Function5<T1, T2, T3, T4, T5, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b), codec4.decode(b), codec5.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                    codec4.encode(b, from4.apply(c));
                    codec5.encode(b, from5.apply(c));
                }
            };
        }

    static <B extends ByteBuf, C, T1, T2, T3, T4, T5, T6> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            Function6<T1, T2, T3, T4, T5, T6, C> to
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b), codec4.decode(b), codec5.decode(b), codec6.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                    codec4.encode(b, from4.apply(c));
                    codec5.encode(b, from5.apply(c));
                    codec6.encode(b, from6.apply(c));
                }
            };
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
