package ivorius.psychedelicraft.util.compat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public interface ComponentChanges {

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private final NbtCompound nbt = new NbtCompound();

        public <T> Builder add(ComponentType<T> type, T value) {
            type.codec().encodeStart(NbtOps.INSTANCE, value).result().ifPresent(data -> {
                nbt.put(type.id().toUnderscoreSeparatedString(), data);
            });
            return this;
        }

        public NbtCompound build() {
            return nbt;
        }
    }
}
