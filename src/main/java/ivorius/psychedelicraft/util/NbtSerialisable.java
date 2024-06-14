package ivorius.psychedelicraft.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public interface NbtSerialisable {

    default NbtCompound toNbt(WrapperLookup lookup) {
        NbtCompound tagCompound = new NbtCompound();
        toNbt(tagCompound, lookup);
        return tagCompound;
    }

    void toNbt(NbtCompound compound, WrapperLookup lookup);

    void fromNbt(NbtCompound compound, WrapperLookup lookup);
}
