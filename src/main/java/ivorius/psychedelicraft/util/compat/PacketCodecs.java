package ivorius.psychedelicraft.util.compat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public interface PacketCodecs {
    PacketCodec<ByteBuf, String> STRING = PacketCodec.ofStatic((buff, s) -> new PacketByteBuf(buff).writeString(s), buff -> new PacketByteBuf(buff).readString());
    PacketCodec<ByteBuf, Integer> INTEGER = PacketCodec.ofStatic(ByteBuf::writeInt, ByteBuf::readInt);
    PacketCodec<ByteBuf, Float> FLOAT = PacketCodec.ofStatic(ByteBuf::writeFloat, ByteBuf::readFloat);
    PacketCodec<ByteBuf, Double> DOUBLE = PacketCodec.ofStatic(ByteBuf::writeDouble, ByteBuf::readDouble);
    PacketCodec<ByteBuf, Long> VAR_LONG = PacketCodec.ofStatic(ByteBuf::writeLong, ByteBuf::readLong);
    PacketCodec<ByteBuf, Boolean> BOOL = PacketCodec.ofStatic(ByteBuf::writeBoolean, ByteBuf::readBoolean);

    PacketCodec<ByteBuf, Vector3f> VECTOR3F = PacketCodec.tuple(FLOAT, Vector3f::x, FLOAT, Vector3f::y, FLOAT, Vector3f::z, Vector3f::new);

    PacketCodec<PacketByteBuf, NbtElement> NBT_ELEMENT = PacketCodec.ofStatic((buffer, nbt) -> {
        if (nbt == null) {
            buffer.writeByte(0);
        } else {
            try {
                NbtIo.write(nbt, new ByteBufOutputStream(buffer));
            } catch (IOException e) {
                throw new EncoderException(e);
            }
        }
    }, buffer -> {
        try {
            ByteBufInputStream input = new ByteBufInputStream(buffer);
            byte type = input.readByte();
            if (type == 0) {
                return null;
            }
            NbtString.skip(input);
            return NbtTypes.byId(type).read(input, 0, NbtTagSizeTracker.EMPTY);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    });
    PacketCodec<ByteBuf, Identifier> IDENTIFIER = STRING.xmap(Identifier::new, Identifier::toString);
    PacketCodec<PacketByteBuf, Ingredient> INGREDIENT = PacketCodec.ofStatic((buff, i) -> i.write(buff), Ingredient::fromPacket);
    PacketCodec<PacketByteBuf, ItemStack> ITEM_STACK = PacketCodec.ofStatic(PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);

    PacketCodec<PacketByteBuf, Optional<ItemStack>> OPTIONAL_ITEM_STACK = optional(ITEM_STACK);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T> PacketCodec<ByteBuf, T> registryValue(RegistryKey<? extends Registry<T>> key) {
        return IDENTIFIER.<T>xmap(
                id -> (T)net.minecraft.registry.Registries.REGISTRIES.get((RegistryKey)key).get(id),
                obj -> ((Registry<T>)net.minecraft.registry.Registries.REGISTRIES.get((RegistryKey)key)).getId(obj)
        );
    }

    static <Buff extends ByteBuf, V> PacketCodec<Buff, Optional<V>> optional(PacketCodec<? super Buff, V> valueCodec) {
        return PacketCodec.ofStatic(
                (buffer, value) -> {
                    buffer.writeBoolean(value.isPresent());
                    if (value.isPresent()) {
                        valueCodec.encode(buffer, value.get());
                    }
                },
                buffer -> {
                    return buffer.readBoolean() ? Optional.of(valueCodec.decode(buffer)) : Optional.empty();
                }
        );
    }

    static<B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList() {
        return PacketCodecs.toCollection(ArrayList::new);
    }

    static <K, V> PacketCodec<PacketByteBuf, Map<K, V>> map(IntFunction<Map<K, V>> maker, PacketCodec<? super PacketByteBuf, K> keyCodec, PacketCodec<? super PacketByteBuf, V> valueCodec) {
        return PacketCodec.ofStatic(
                (buffer, value) -> buffer.writeMap(value, keyCodec::encode, valueCodec::encode),
                buffer -> buffer.readMap(maker, keyCodec::decode, valueCodec::decode)
        );
    }

    static <Buff extends ByteBuf, T, Result extends Collection<T>> PacketCodec.ResultFunction<Buff, T, Result> toCollection(IntFunction<Result> factory) {
        return new PacketCodec.ResultFunction<>() {
            @Override
            public void encode(Buff buffer, Result value, PacketCodec<? super Buff, T> elementCodec) {
                buffer.writeInt(value.size());
                for (T t : value) {
                    elementCodec.encode(buffer, t);
                }
            }

            @Override
            public Result decode(Buff buffer, PacketCodec<? super Buff, T> elementCodec) {
                int size = buffer.readInt();
                var c = factory.apply(size);
                for (int i = 0; i < size; i++) {
                    c.add(elementCodec.decode(buffer));
                }
                return c;
            }
        };
    }
}
