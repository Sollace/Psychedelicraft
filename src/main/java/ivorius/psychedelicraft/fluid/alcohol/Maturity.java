package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Locale;

import net.minecraft.text.Text;

public enum Maturity {
    YOUNG,
    AGED,
    MATURE,
    VERY_MATURE;

    private static final Maturity[] STAGES = {
            YOUNG, AGED, MATURE, VERY_MATURE, VERY_MATURE
    };

    public static Maturity getMaturity(int maturation) {
        return STAGES[(maturation / (STAGES.length - 1)) % STAGES.length];
    }

    private final Text name = Text.translatable("psychedelicraft.alcohol.maturity." + name().toLowerCase(Locale.ROOT));

    public Text getName() {
        return name;
    }
}
