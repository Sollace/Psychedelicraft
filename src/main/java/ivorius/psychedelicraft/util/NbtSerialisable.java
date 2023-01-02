package ivorius.psychedelicraft.util;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerialisable {

    default NbtCompound toNbt() {
        NbtCompound tagCompound = new NbtCompound();
        toNbt(tagCompound);
        return tagCompound;
    }

    void toNbt(NbtCompound compound);

    void fromNbt(NbtCompound compound);
}
