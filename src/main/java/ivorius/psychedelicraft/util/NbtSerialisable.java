package ivorius.psychedelicraft.util;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public interface NbtSerialisable {

    default NbtCompound toNbt() {
        NbtCompound tagCompound = new NbtCompound();
        toNbt(tagCompound);
        return tagCompound;
    }

    void toNbt(NbtCompound compound);

    void fromNbt(NbtCompound compound);

    static <T extends NbtSerialisable> NbtList fromList(List<T> list) {
        NbtList nbt = new NbtList();
        list.forEach(t -> nbt.add(t.toNbt()));
        return nbt;
    }

    static <T extends NbtSerialisable> List<T> toList(List<T> list, NbtList nbt, Supplier<T> supplier) {
        list.clear();
        nbt.forEach(element -> {
            T t = supplier.get();
            t.fromNbt((NbtCompound)element);
            list.add(t);
        });
        return list;
    }
}
