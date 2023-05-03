package ivorius.psychedelicraft.fluid.alcohol;

public record FluidAppearance (
        String still,
        String flowing
    ) {
    public static final FluidAppearance CLEAR = of("clear");
    public static final FluidAppearance TEA = of("tea");
    public static final FluidAppearance COFFEE = of("coffee");
    public static final FluidAppearance BEER = of("beer");
    public static final FluidAppearance WINE = of("wine");
    public static final FluidAppearance RICE_WINE = of("rice_wine");
    public static final FluidAppearance SLURRY = of("slurry");
    public static final FluidAppearance CIDER = of("cider");
    public static final FluidAppearance MEAD = of("mead");
    public static final FluidAppearance TOMATO_JUICE = of("tomato_juice");
    public static final FluidAppearance KETCHUP = of("ketchup");
    public static final FluidAppearance RUM_SEMI_MATURE = of("rum_semi_mature");
    public static final FluidAppearance RUM_MATURE = of("rum_mature");

    public static FluidAppearance of(String name) {
        return new FluidAppearance(name + "_still", name + "_flow");
    }
}