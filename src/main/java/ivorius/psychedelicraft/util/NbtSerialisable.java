package ivorius.psychedelicraft.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public interface NbtSerialisable {

    default NbtCompound toNbt(WrapperLookup lookup) {
        NbtCompound tagCompound = new NbtCompound();
        toNbt(tagCompound, lookup);
        return tagCompound;
    }

    void toNbt(NbtCompound compound, WrapperLookup lookup);

    void fromNbt(NbtCompound compound, WrapperLookup lookup);

    static <T> void put(NbtCompound nbt, String key, Codec<T> codec, T value) {
        nbt.put(key, codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow());
    }

    static <T> void putNullable(NbtCompound nbt, String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            put(nbt, key, codec, value);
        }
    }

    static <T> Optional<T> get(NbtCompound nbt, String key, Codec<T> codec) {
        return nbt.contains(key) ? codec.decode(NbtOps.INSTANCE, nbt.get(key)).result().map(Pair::getFirst) : Optional.empty();
    }

    static <T extends NbtSerialisable> NbtList fromList(List<T> list, WrapperLookup lookup) {
        NbtList nbt = new NbtList();
        list.forEach(t -> nbt.add(t.toNbt(lookup)));
        return nbt;
    }

    static <T extends NbtSerialisable> List<T> toList(List<T> list, NbtList nbt, WrapperLookup lookup, Supplier<T> supplier) {
        list.clear();
        nbt.forEach(element -> {
            T t = supplier.get();
            t.fromNbt((NbtCompound)element, lookup);
            list.add(t);
        });
        return list;
    }
}
