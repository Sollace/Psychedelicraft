package ivorius.psychedelicraft.entity.drug;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.nbt.NbtCompound;

public interface LockableHungerManager {

    default void lockHunger(int hunger, float saturation, boolean full) {
        setLockedState(new State(hunger, saturation, full));
    }

    default void lockHunger(boolean full) {
        lockHunger(getHungerManager().getFoodLevel(), getHungerManager().getSaturationLevel(), full);
    }

    default void unlockHunger() {
        setLockedState(null);
    }

    HungerManager getHungerManager();

    @Nullable
    State getLockedState();

    void setLockedState(State state);

    record State(int hunger, float saturation, boolean full) {
        static State fromNbt(NbtCompound compound) {
            return new State(compound.getInt("hunger"), compound.getFloat("saturation"), compound.getBoolean("full"));
        }

        public NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.putInt("hunger", hunger);
            compound.putFloat("saturation", saturation);
            compound.putBoolean("full", full);
            return compound;
        }
    }
}
