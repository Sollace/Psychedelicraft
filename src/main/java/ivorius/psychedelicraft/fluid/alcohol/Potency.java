package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Locale;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public enum Potency {
    WATERY,
    WEAK,
    STRONG,
    AROMATIC,
    PUNGENT;

    private static final Potency[] VALUES = values();

    public Potency applyState(int distillation, int fermentation, int maturation) {
        int potency = (int)MathHelper.clamp(
            (distillation / 16F) - ((fermentation / 16F) / 3F) + ((maturation / 16F) / 2F) + ordinal(), 0, VALUES.length
        ) % VALUES.length;
        return VALUES[potency];
    }

    private final Text name = Text.translatable("psychedelicraft.alcohol.potency." + name().toLowerCase(Locale.ROOT));

    public Text getName() {
        return name;
    }
}
