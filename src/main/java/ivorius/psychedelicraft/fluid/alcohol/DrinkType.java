package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.minecraft.component.ComponentType;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record DrinkType(String drinkName, String symbolName, Optional<String> variant, Optional<DrugInfluence> extraDrug, FluidAppearance appearance) {
    public static final DrinkType ROOT = of("");
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
        return new DrinkType(drinkName, drinkName, Optional.empty(), Optional.empty(), FluidAppearance.CLEAR);
    }

    public static DrinkType of(String drinkName, DrinkType looksLike) {
        return new DrinkType(drinkName, looksLike.symbolName(), looksLike.variant(), looksLike.extraDrug(), looksLike.appearance());
    }

    public DrinkType withVariation(String variation) {
        return new DrinkType(drinkName, symbolName, Optional.of(variation), extraDrug, appearance);
    }

    public DrinkType withSymbol(String symbolName) {
        return new DrinkType(drinkName, symbolName, variant, extraDrug, appearance);
    }

    public DrinkType withName(String drinkName) {
        return new DrinkType(drinkName, symbolName, variant, extraDrug, appearance);
    }

    public DrinkType withAppearance(FluidAppearance appearance) {
        return new DrinkType(drinkName, symbolName, variant, extraDrug, appearance);
    }

    public DrinkType withExtraDrug(DrugInfluence extraDrug) {
        return new DrinkType(drinkName, symbolName, variant, Optional.of(extraDrug), appearance);
    }

    public String getUniqueKey() {
        return Stream.concat(Stream.of(appearance.name(), drinkName), variant.stream()).collect(Collectors.joining("_"));
    }

    public boolean isOf(DrinkType type) {
        return drinkName.equalsIgnoreCase(type.drinkName());
    }

    public Text getName(Text fluidName) {
        Text name = drinkName.isEmpty() ? fluidName : Text.translatable("psychedelicraft.alcohol.drink." + drinkName, fluidName);

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

    public record Predicate(Optional<ItemFluids.Predicate> fluids, DrinkType name) implements ComponentSubPredicate<ItemFluids> {
        public static final Codec<Predicate> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemFluids.Predicate.CODEC.optionalFieldOf("fluids").forGetter(Predicate::fluids),
                Codec.STRING.xmap(DrinkType::of, DrinkType::drinkName).fieldOf("drink_name").forGetter(Predicate::name)
        ).apply(i, Predicate::new));

        public static Predicate create(DrinkType name) {
            return new DrinkType.Predicate(Optional.empty(), name);
        }

        public static Predicate create(DrinkType name, SimpleFluid...fluid) {
            return new DrinkType.Predicate(Optional.of(ItemFluids.Predicate.builder().fluid(fluid).build()), name);
        }

        @Override
        public ComponentType<ItemFluids> getComponentType() {
            return PSComponents.FLUIDS;
        }

        @Override
        public boolean test(ItemFluids fluids) {
            return (this.fluids.isEmpty() || this.fluids.get().test(fluids))
                    && fluids.fluid() instanceof AlcoholicFluid alco
                    && alco.getVariant(fluids).isOf(name);
        }
    }
}