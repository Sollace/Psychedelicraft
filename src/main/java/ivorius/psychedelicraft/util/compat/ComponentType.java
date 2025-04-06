package ivorius.psychedelicraft.util.compat;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record ComponentType<T>(Identifier id, Codec<T> codec, PacketCodec<?, T> packetCodec, Map<Item, T> itemDefaults) {
    static final Map<Identifier, ComponentType<?>> REGISTRY = new HashMap<>();

    public static <T> ComponentType<T> register(Identifier id, Builder<T> builder) {
        var type = builder.build(id);
        REGISTRY.put(id, type);
        return type;
    }

    public static <T extends Item, V> T add(T item, ComponentType<V> type, V value) {
        type.itemDefaults().put(item, value);
        return item;
    }

    public static class Builder<T> {
        private Codec<T> codec;
        private PacketCodec<?, T> packetCodec;

        public Builder<T> codec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> packetCodec(PacketCodec<?, T> packetCodec) {
            this.packetCodec = packetCodec;
            return this;
        }

        ComponentType<T> build(Identifier id) {
            return new ComponentType<>(id, codec, packetCodec, new HashMap<>());
        }
    }
}
