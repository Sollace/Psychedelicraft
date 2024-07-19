package ivorius.psychedelicraft.util;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public interface NbtSerialisable {

    default NbtCompound toNbt(WrapperLookup lookup) {
        NbtCompound tagCompound = new NbtCompound();
        toNbt(tagCompound, lookup);
        return tagCompound;
    }

    void toNbt(NbtCompound compound, WrapperLookup lookup);

    void fromNbt(NbtCompound compound, WrapperLookup lookup);

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
