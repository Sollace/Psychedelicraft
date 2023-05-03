package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DynamicallyNamedAlcoholicFluid extends AlcoholicFluid {

    private final DrinkType defaultDrinkName;

    public DynamicallyNamedAlcoholicFluid(Identifier id, String defaultDrinkName, Settings settings) {
        this(id, DrinkType.of(defaultDrinkName), settings);
    }

    public DynamicallyNamedAlcoholicFluid(Identifier id, DrinkType defaultDrinkName, Settings settings) {
        super(id, settings);
        this.defaultDrinkName = defaultDrinkName;
    }


    @Override
    public Text getName(ItemStack stack) {

        var specialName = settings.variants.find(stack);
        if (specialName == null) {
            specialName = defaultDrinkName;
        }

        return specialName.getName(Text.translatable(getTranslationKey()));
    }
}
