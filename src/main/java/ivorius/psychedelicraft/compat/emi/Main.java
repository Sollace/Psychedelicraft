package ivorius.psychedelicraft.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@Deprecated
public class Main implements EmiPlugin {

    /*static final Comparison COMPARE_FLUID = Comparison.of(
            (a, b) -> SimpleFluid.isEquivalent(RecipeUtil.getFluid(a), a.getItemStack(), RecipeUtil.getFluid(b), b.getItemStack()),
            s -> RecipeUtil.getFluid(s).getHash(s.getItemStack())
    );*/

    @Override
    public void register(EmiRegistry registry) {
        //RecipeCategory.initialize(registry);

        /*registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING).forEach(recipe -> {
            if (recipe.value() instanceof FluidAwareShapelessRecipe r) {
                var input = r.getFluidAwareIngredients().stream().map(RecipeUtil::convertIngredient).toList();
                EmiShapedRecipe.setRemainders(input, r);
                RecipeUtil.replaceRecipe(registry, new EmiCraftingRecipe(input, EmiStack.of(EmiPort.getOutput(r)), recipe.id()) {
                    @Override
                    public boolean canFit(int width, int height) {
                        return input.size() <= width * height;
                    }
                });
            }

            if (recipe.value() instanceof BottleRecipe r) {
                ItemStack output = EmiPort.getOutput(r);

                if (output.get(DataComponentTypes.DYED_COLOR) != null) {
                    var input = RecipeUtil.padIngredients(r);
                    EmiShapedRecipe.setRemainders(input, r);
                    RecipeUtil.replaceRecipe(registry, BottleRecipe.COLORS.entrySet().stream().map(variation -> {
                        ItemStack result = output.copy();
                        result.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(variation.getValue().getSignColor(), true));
                        return new EmiCraftingRecipe(
                                input,
                                EmiStack.of(result),
                                recipe.id().withPath(p -> p + "/" + variation.getValue().asString()),
                                false
                        );
                    }).toArray(EmiRecipe[]::new));
                }
            }

            if (recipe.value() instanceof PouringRecipe) {
                // TODO:
            }
            if (recipe.value() instanceof ChangeRecepticalRecipe) {
                // TODO:
            }
            if (recipe.value() instanceof FillRecepticalRecipe r) {
                RecipeUtil.replaceRecipe(registry, new FluidCraftingEmiRecipe(recipe.id(), r));
            }
        });*/

        /*registry.getRecipeManager().listAllOfType(RecipeType.SMELTING).forEach(recipe -> {
            if (recipe.value() instanceof SmeltingFluidRecipe r) {
                RecipeUtil.replaceRecipe(registry, new SmeltingFluidEmiRecipe(recipe.id(), r));
            }
        });*/

        /*
        registry.addRecipe(createInteraction("morning_glory_flowers", EmiStack.of(PSItems.MORNING_GLORY_LATTICE), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.MORNING_GLORY)));
        registry.addRecipe(createInteraction("juniper_berries", EmiStack.of(PSItems.FRUITING_JUNIPER_LEAVES), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.JUNIPER_BERRIES)));
        registry.addRecipe(createInteraction("wine_grapes", EmiStack.of(PSItems.WINE_GRAPE_LATTICE), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.WINE_GRAPES)));
        */

        /*SimpleFluid.REGISTRY.forEach(fluid -> {
            if (!fluid.isEmpty()) {
                var contents = DrawingFluidEmiRecipe.Contents.of(fluid);
                registry.addRecipe(new DrawingFluidEmiRecipe(RecipeCategory.BARREL.category(), fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(RecipeCategory.DISTILLERY.category(), fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(RecipeCategory.FLASK.category(), fluid, contents));

                if (fluid instanceof Processable p) {
                    Stream.of(
                            FluidProcessingEmiRecipe.createRecipesFor(RecipeCategory.MATURING.category(), ProcessType.MATURE, p, fluid),
                            FluidProcessingEmiRecipe.createRecipesFor(RecipeCategory.DISTILLING.category(), ProcessType.DISTILL, p, fluid),
                            FluidProcessingEmiRecipe.createRecipesFor(RecipeCategory.FERMENTING.category(), ProcessType.FERMENT, p, fluid)
                    ).flatMap(Function.identity()).forEach(registry::addRecipe);
                }
            }

            setComparison(registry, COMPARE_FLUID, RecipeUtil.getStackKey(fluid));
        });

        setComparison(registry, COMPARE_FLUID,
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        );*/
    }

    /*static void setComparison(EmiRegistry registry, Comparison comparison, Object...keys) {
        for (var key : keys) {
            registry.setDefaultComparison(key, c -> comparison);
        }
    }*/

    /*static EmiRecipe createInteraction(String name, EmiIngredient left, EmiIngredient right, EmiStack output) {
        return EmiWorldInteractionRecipe.builder()
                .id(Psychedelicraft.id(name))
                .leftInput(left)
                .rightInput(right, true)
                .output(output)
                .build();
    }*/
}
