package ivorius.psychedelicraft.entity.drug;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public interface LockableHungerManager {
    default void lockHunger(int hunger, float saturation, boolean full, float strength) {
        setLockedState(new State(new Ratio(hunger, strength), new Ratio(saturation, strength), full));
    }

    default void lockHunger(boolean full, float ratio) {
        lockHunger(getHungerManager().getFoodLevel(), getHungerManager().getSaturationLevel(), full, ratio);
    }

    default void unlockHunger() {
        setLockedState(null);
    }

    default void makePermanent() {
        if (getLockedState() != null) {
            HungerManager hunger = getHungerManager();
            hunger.setSaturationLevel(hunger.getSaturationLevel());
            hunger.setFoodLevel(hunger.getFoodLevel());
            unlockHunger();
        }
    }

    HungerManager getHungerManager();

    @Nullable
    State getLockedState();

    void setLockedState(State state);

    record State(Ratio hunger, Ratio saturation, boolean full) {
        static State fromNbt(NbtCompound compound) {
            return new State(
                    Ratio.fromNbt(compound.getCompound("hunger")),
                    Ratio.fromNbt(compound.getCompound("saturation")),
                    compound.getBoolean("full")
            );
        }

        public NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.put("hunger", hunger.toNbt());
            compound.put("saturation", saturation.toNbt());
            compound.putBoolean("full", full);
            return compound;
        }

        public void setRate(float rate) {
            hunger.setRate(rate);
            saturation.setRate(rate);
        }
    }

    final class Ratio {
        private float initial;
        private float rate;

        Ratio(float initial, float rate) {
            this.initial = initial;
            this.rate = rate;
        }

        public void setRate(float newRate) {
            this.rate = newRate;
        }

        public float getRate() {
            return rate;
        }

        static Ratio fromNbt(NbtCompound compound) {
            return new Ratio(compound.getFloat("initial"), compound.getFloat("rate"));
        }

        public float toFloat(float reference) {
            initial = rate > 0 ? Math.max(initial, reference) : Math.min(initial, reference);

            return Math.max(0, MathHelper.lerp(reference > initial ? -rate : rate, reference, initial));
        }

        public NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.putFloat("initial", initial);
            compound.putFloat("rate", rate);
            return compound;
        }
    }
}
