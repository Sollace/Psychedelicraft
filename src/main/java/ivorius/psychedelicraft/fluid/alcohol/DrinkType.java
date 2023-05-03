package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Optional;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record DrinkType(String drinkName, String symbolName, Optional<String> variant, FluidAppearance appearance) {
    public static final DrinkType TEA = of("tea").withAppearance(FluidAppearance.TEA);
    public static final DrinkType JUICE = of("juice");
    public static final DrinkType WORT = of("wort").withAppearance(FluidAppearance.BEER);
    public static final DrinkType VINEGAR = of("vinegar");
    public static final DrinkType BLAAND = of("blaand");
    public static final DrinkType ARKHI = of("arkhi");
    public static final DrinkType MEAD = of("mead").withAppearance(FluidAppearance.MEAD);
    public static final DrinkType WINE = of("wine").withAppearance(FluidAppearance.WINE);
    public static final DrinkType BRANDY = of("brandy").withAppearance(FluidAppearance.RUM_SEMI_MATURE);
    public static final DrinkType CIDER = of("cider").withAppearance(FluidAppearance.CIDER);
    public static final DrinkType POTEEN = of("poteen");
    public static final DrinkType GIN = of("gin");
    public static final DrinkType VODKA = of("vodka");
    public static final DrinkType WHISKEY = of("whiskey");
    public static final DrinkType KETCHUP = of("ketchup").withAppearance(FluidAppearance.KETCHUP);
    public static final DrinkType WASH = of("wash").withAppearance(FluidAppearance.RUM_MATURE);
    public static final DrinkType HALF_WASH = of("half_wash").withAppearance(FluidAppearance.RUM_SEMI_MATURE);
    public static final DrinkType BEER = of("beer").withAppearance(FluidAppearance.BEER);
    public static final DrinkType BASI = of("basi");
    public static final DrinkType RUM = of("rum").withAppearance(FluidAppearance.RUM_SEMI_MATURE);
    public static final DrinkType MEZCAL = of("mezcal").withAppearance(FluidAppearance.RUM_SEMI_MATURE);
    public static final DrinkType TEQUILA = of("tequila");

    public static DrinkType of(String drinkName) {
        return new DrinkType(drinkName, drinkName, Optional.empty(), FluidAppearance.CLEAR);
    }

    public static DrinkType of(String drinkName, DrinkType looksLike) {
        return new DrinkType(drinkName, looksLike.symbolName(), looksLike.variant(), looksLike.appearance());
    }

    public DrinkType withVariation(String variation) {
        return new DrinkType(drinkName, symbolName, Optional.of(variation), appearance);
    }

    public DrinkType withSymbol(String symbolName) {
        return new DrinkType(drinkName, symbolName, variant, appearance);
    }

    public DrinkType withName(String drinkName) {
        return new DrinkType(drinkName, symbolName, variant, appearance);
    }

    public DrinkType withAppearance(FluidAppearance appearance) {
        return new DrinkType(drinkName, symbolName, variant, appearance);
    }

    public String getUniqueKey() {
        return variant.map(v -> v + drinkName).orElse(drinkName);
    }

    public Text getName(Text fluidName) {
        Text name = Text.translatable("psychedelicraft.alcohol.drink." + drinkName, fluidName);

        if (variant.isPresent()) {
            return Text.translatable("psychedelicraft.alcohol.drink.variant." + variant.get(), name);
        }
        return name;
    }

    public Identifier getSymbol(Identifier fluidId) {
        return fluidId.withPath(p -> "textures/fluid/" + symbolName + ".png");
    }

    public interface Variation {
        String YOUNG = "young";
        String HARD = "hard";
        String GREEN = "green";
        String BITTER = "bitter";
        String SWEET = "sweet";
        String HALF_SWEET = "half_sweet";
        String AGED = "aged";
        String SLIGHTLY_AGED = "slightly_aged";
        String WELL_AGED = "well_aged";
        String BLANCO = "blanco";
        String REPOSADO = "reposado";
        String WINE = "wine";
    }
}