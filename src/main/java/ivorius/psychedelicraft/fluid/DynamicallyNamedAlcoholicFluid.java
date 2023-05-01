package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DynamicallyNamedAlcoholicFluid extends AlcoholicFluid {

    public DynamicallyNamedAlcoholicFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public Text getName(ItemStack stack) {

        var specialName = settings.displayNames.find(stack);
        if (specialName == null && DISTILLATION.get(stack) == 0 && MATURATION.get(stack) == 0 && FERMENTATION.get(stack) == 0) {
            specialName = "juice";
        }

        if (specialName == null) {
            return super.getName(stack);
        }

        return Text.translatable("psychedelicraft.alcohol.drink." + specialName, Text.translatable(getTranslationKey()));
    }
}
