package ivorius.psychedelicraft.fluid.alcohol;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record FlavorProfile (Charisma charisma, Potency potency, Texture texture) {
    public static final FlavorProfile DEFAULT = new FlavorProfile(Charisma.PLAIN, Potency.WATERY, Texture.SMOOTH);

    public Text getFlavour(int distillation, int fermentation, int maturation) {
        Charisma charisma = this.charisma.applyState(distillation, fermentation, maturation);
        Potency potency = this.potency.applyState(distillation, fermentation, maturation);
        Texture texture = this.texture.applyState(distillation, fermentation, maturation);
        Maturity maturity = Maturity.getMaturity(maturation);

        return Text.translatable("psychedelicract.alcohol.flavor",
                texture.getName(), charisma.getName(),
                potency.getName(), maturity.getName()
        ).formatted(Formatting.DARK_AQUA, Formatting.ITALIC);
    }
}
