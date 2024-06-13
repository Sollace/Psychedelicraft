package ivorius.psychedelicraft.compat.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiShapedRecipe;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.BottleRecipe;
import ivorius.psychedelicraft.recipe.ChangeRecepticalRecipe;
import ivorius.psychedelicraft.recipe.FillRecepticalRecipe;
import ivorius.psychedelicraft.recipe.FluidAwareShapelessRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.PouringRecipe;
import ivorius.psychedelicraft.recipe.SmeltingFluidRecipe;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;

public class Main implements EmiPlugin {
    static final RecipeCategory DRYING_TABLE = RecipeCategory.register("drying_table", EmiStack.of(PSItems.DRYING_TABLE), registry -> {
        registry.getRecipeManager().listAllOfType(PSRecipes.DRYING_TYPE).forEach(recipe -> {
            registry.addRecipe(new DryingEmiRecipe(recipe.id(), recipe.value()));
        });
    });
    static final RecipeCategory VAT = RecipeCategory.register("wooden_vat", EmiStack.of(PSItems.MASH_TUB), registry -> {
        registry.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).forEach(recipe -> {
            registry.addRecipe(new MashingEmiRecipe(recipe.id(), recipe.value()));
        });
    });
    static final RecipeCategory BARREL = RecipeCategory.register("barrel", EmiStack.of(PSItems.OAK_BARREL), EmiIngredient.of(PSTags.BARRELS), registry -> {});
    static final RecipeCategory DISTILLERY = RecipeCategory.register("distillery", EmiStack.of(PSItems.DISTILLERY), registry -> {});
    static final RecipeCategory FLASK = RecipeCategory.register("flask", EmiStack.of(PSItems.FLASK), registry -> {});

    static final RecipeCategory FERMENTING = RecipeCategory.register("fermenting", EmiStack.of(PSItems.MASH_TUB), registry -> {});
    static final RecipeCategory MATURING = RecipeCategory.register("maturing", EmiStack.of(PSItems.OAK_BARREL), EmiIngredient.of(PSTags.BARRELS), registry -> {});
    static final RecipeCategory DISTILLING = RecipeCategory.register("distilling", EmiStack.of(PSItems.DISTILLERY), registry -> {});

    static final Comparison COMPARE_FLUID = Comparison.of(
            (a, b) -> SimpleFluid.isEquivalent(RecipeUtil.getFluid(a), a.getItemStack(), RecipeUtil.getFluid(b), b.getItemStack()),
            s -> RecipeUtil.getFluid(s).getHash(s.getItemStack())
    );

    @Override
    public void register(EmiRegistry registry) {
        RecipeCategory.initialize(registry);

        registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING).forEach(recipe -> {
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
        });

        registry.getRecipeManager().listAllOfType(RecipeType.SMELTING).forEach(recipe -> {
            if (recipe.value() instanceof SmeltingFluidRecipe r) {
                RecipeUtil.replaceRecipe(registry, new SmeltingFluidEmiRecipe(recipe.id(), r));
            }
        });

        registry.addRecipe(createInteraction("morning_glory_flowers", EmiStack.of(PSItems.MORNING_GLORY_LATTICE), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.MORNING_GLORY)));
        registry.addRecipe(createInteraction("juniper_berries", EmiStack.of(PSItems.FRUITING_JUNIPER_LEAVES), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.JUNIPER_BERRIES)));
        registry.addRecipe(createInteraction("wine_grapes", EmiStack.of(PSItems.WINE_GRAPE_LATTICE), EmiIngredient.of(ConventionalItemTags.SHEAR_TOOLS), EmiStack.of(PSItems.WINE_GRAPES)));

        SimpleFluid.all().forEach(fluid -> {
            if (!fluid.isEmpty()) {
                var contents = DrawingFluidEmiRecipe.Contents.of(fluid);
                registry.addRecipe(new DrawingFluidEmiRecipe(BARREL.category(), fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(DISTILLERY.category(), fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(FLASK.category(), fluid, contents));

                if (fluid instanceof Processable p) {
                    FluidProcessingEmiRecipe.createRecipesFor(MATURING.category(), ProcessType.MATURE, p, fluid, registry::addRecipe);
                    FluidProcessingEmiRecipe.createRecipesFor(DISTILLING.category(), ProcessType.DISTILL, p, fluid, registry::addRecipe);
                    FluidProcessingEmiRecipe.createRecipesFor(FERMENTING.category(), ProcessType.FERMENT, p, fluid, registry::addRecipe);
                }
            }

            setComparison(registry, COMPARE_FLUID, RecipeUtil.getStackKey(fluid));
        });

        setComparison(registry, COMPARE_FLUID,
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        );
    }

    static void setComparison(EmiRegistry registry, Comparison comparison, Object...keys) {
        for (var key : keys) {
            registry.setDefaultComparison(key, c -> comparison);
        }
    }

    static EmiRecipe createInteraction(String name, EmiIngredient left, EmiIngredient right, EmiStack output) {
        return EmiWorldInteractionRecipe.builder()
                .id(Psychedelicraft.id(name))
                .leftInput(left)
                .rightInput(right, true)
                .output(output)
                .build();
    }
}
