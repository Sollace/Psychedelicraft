package ivorius.psychedelicraft.util;

import net.minecraft.util.math.MathHelper;

public interface MathUtils {
    static float nearValue(float from, float to, float delta, float plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static float approach(float value, float target, float adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static double nearValue(double from, double to, double delta, double plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static double approach(double value, double target, double adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }
}
