package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Locale;

import net.minecraft.text.Text;

public enum Charisma {
    PLAIN,
    FRUITY,
    ACIDIC,
    ACRID;

    public Charisma applyState(int distillation, int fermentation, int maturation) {
        float percentPlain = (distillation / 16F) / 2F; // 0-0.5
        float percentAcrid = maturation / 16F; // 0-1
        float percentBase = 1 - (percentPlain + percentAcrid); //0.25-1

        if (!isWinner(percentBase, percentPlain, percentAcrid)) {
            if (isWinner(percentPlain, percentBase, percentAcrid)) {
                return PLAIN;
            }

            if (isWinner(percentAcrid, percentPlain, percentBase)) {
                return getAcrid(percentAcrid);
            }
        }

        if (this == ACIDIC) {
            return getAcrid(percentBase);
        }
        return this;
    }

    private static Charisma getAcrid(float acidity) {
        return acidity > 0.5 ? ACRID : ACIDIC;
    }

    private static boolean isWinner(float competator, float opponent1, float opponent2) {
        return competator > opponent1 && competator > opponent2;
    }

    private final Text name = Text.translatable("psychedelicraft.alcohol.charisma." + name().toLowerCase(Locale.ROOT));

    public Text getName() {
        return name;
    }
}
