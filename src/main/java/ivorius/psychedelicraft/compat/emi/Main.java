package ivorius.psychedelicraft.compat.emi;

import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
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
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.BottleRecipe;
import ivorius.psychedelicraft.recipe.ChangeRecepticalRecipe;
import ivorius.psychedelicraft.recipe.FillRecepticalRecipe;
import ivorius.psychedelicraft.recipe.FluidAwareShapelessRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.PouringRecipe;
import ivorius.psychedelicraft.recipe.SmeltingFluidRecipe;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public class Main implements EmiPlugin {
    static final EmiStack DRYING_TABLE_STATION = EmiStack.of(PSItems.DRYING_TABLE);
    static final EmiRecipeCategory DRYING_TABLE_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("drying_table"), DRYING_TABLE_STATION, DRYING_TABLE_STATION);

    static final EmiStack VAT_STATION = EmiStack.of(PSItems.MASH_TUB);
    static final EmiRecipeCategory VAT_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("wooden_vat"), VAT_STATION, VAT_STATION);
    static final EmiRecipeCategory FERMENTING_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("fermenting"), VAT_STATION, VAT_STATION);

    static final EmiStack BARREL_STATION = EmiStack.of(PSItems.OAK_BARREL);
    static final EmiRecipeCategory BARREL_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("barrel"), BARREL_STATION, BARREL_STATION);
    static final EmiRecipeCategory MATURING_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("maturing"), BARREL_STATION, BARREL_STATION);

    static final EmiStack DISTILLERY_STATION = EmiStack.of(PSItems.DISTILLERY);
    static final EmiRecipeCategory DISTILLERY_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("distillery"), DISTILLERY_STATION, DISTILLERY_STATION);
    static final EmiRecipeCategory DISTILLING_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("distilling"), DISTILLERY_STATION, DISTILLERY_STATION);

    static final EmiStack FLASK_STATION = EmiStack.of(PSItems.FLASK);
    static final EmiRecipeCategory FLASK_CATEGORY = new EmiRecipeCategory(Psychedelicraft.id("flask"), FLASK_STATION, FLASK_STATION);

    static final Comparison COMPARE_FLUID = Comparison.of((a, b) -> {
        ItemStack stackA = a.getItemStack();
        ItemStack stackB = b.getItemStack();
        return FluidContainer.of(stackA).getFluid(stackA).isEquivalent(stackA, stackB);
    }, s -> {
        ItemStack stack = s.getItemStack();
        return FluidContainer.of(stack).getFluid(stack).getHash(stack);
    });

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(BARREL_CATEGORY);
        registry.addWorkstation(BARREL_CATEGORY, EmiIngredient.of(PSTags.BARRELS));

        registry.addCategory(MATURING_CATEGORY);
        registry.addWorkstation(MATURING_CATEGORY, EmiIngredient.of(PSTags.BARRELS));

        registry.addCategory(DISTILLERY_CATEGORY);
        registry.addWorkstation(DISTILLERY_CATEGORY, DISTILLERY_STATION);

        registry.addCategory(DISTILLING_CATEGORY);
        registry.addWorkstation(DISTILLING_CATEGORY, DISTILLERY_STATION);

        registry.addCategory(DISTILLING_CATEGORY);
        registry.addWorkstation(DISTILLING_CATEGORY, DISTILLERY_STATION);

        registry.addCategory(FERMENTING_CATEGORY);
        registry.addWorkstation(FERMENTING_CATEGORY, VAT_STATION);

        registry.addCategory(FLASK_CATEGORY);
        registry.addWorkstation(FLASK_CATEGORY, FLASK_STATION);

        registry.addCategory(DRYING_TABLE_CATEGORY);
        registry.addWorkstation(DRYING_TABLE_CATEGORY, EmiIngredient.of(PSTags.DRYING_TABLES));
        registry.getRecipeManager().listAllOfType(PSRecipes.DRYING_TYPE).forEach(recipe -> {
            registry.addRecipe(new DryingEmiRecipe(recipe.getId(), recipe));
        });

        registry.addCategory(VAT_CATEGORY);
        registry.addWorkstation(VAT_CATEGORY, VAT_STATION);
        registry.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).forEach(recipe -> {
            registry.addRecipe(new MashingEmiRecipe(recipe.getId(), recipe));
        });

        registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING).forEach(recipe -> {
            if (recipe instanceof FluidAwareShapelessRecipe r) {
                var input = r.getFluidAwareIngredients().stream().map(RecipeUtil::convertIngredient).toList();
                EmiShapedRecipe.setRemainders(input, r);
                RecipeUtil.replaceRecipe(registry, new EmiCraftingRecipe(input, EmiStack.of(EmiPort.getOutput(r)), recipe.getId()) {
                    @Override
                    public boolean canFit(int width, int height) {
                        return input.size() <= width * height;
                    }
                });
            }
            if (recipe instanceof BottleRecipe r) {
                ItemStack output = EmiPort.getOutput(r);

                if (output.getItem() instanceof DyeableItem dyeable) {
                    var input = RecipeUtil.padIngredients(r);
                    EmiShapedRecipe.setRemainders(input, r);
                    RecipeUtil.replaceRecipe(registry, BottleRecipe.COLORS.entrySet().stream().map(variation -> {
                        dyeable.setColor(output, variation.getValue().getSignColor());
                        return new EmiCraftingRecipe(
                                input,
                                EmiStack.of(output.copy()),
                                new Identifier(recipe.getId().getNamespace(), recipe.getId().getPath() + "/" + variation.getValue().asString()),
                                false
                        );
                    }).toArray(EmiRecipe[]::new));
                }
            }
            if (recipe instanceof PouringRecipe r) {
                // TODO:
            }
            if (recipe instanceof ChangeRecepticalRecipe r) {
                // TODO:
            }
            if (recipe instanceof FillRecepticalRecipe r) {
                RecipeUtil.replaceRecipe(registry, new FluidCraftingEmiRecipe(recipe.getId(), r));
            }
        });

        registry.getRecipeManager().listAllOfType(RecipeType.SMELTING).forEach(recipe -> {
            if (recipe instanceof SmeltingFluidRecipe r) {
                RecipeUtil.replaceRecipe(registry, new SmeltingFluidEmiRecipe(recipe.getId(), r));
            }
        });

        registry.addRecipe(EmiWorldInteractionRecipe.builder()
            .id(Psychedelicraft.id("morning_glory_flowers"))
            .leftInput(EmiStack.of(PSItems.MORNING_GLORY_LATTICE))
            .rightInput(EmiIngredient.of(ConventionalItemTags.SHEARS), true)
            .output(EmiStack.of(PSItems.MORNING_GLORY))
            .build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(Psychedelicraft.id("juniper_berries"))
                .leftInput(EmiStack.of(PSItems.FRUITING_JUNIPER_LEAVES))
                .rightInput(EmiIngredient.of(ConventionalItemTags.SHEARS), true)
                .output(EmiStack.of(PSItems.JUNIPER_BERRIES))
                .build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(Psychedelicraft.id("wine_grapes"))
                .leftInput(EmiStack.of(PSItems.WINE_GRAPE_LATTICE))
                .rightInput(EmiIngredient.of(ConventionalItemTags.SHEARS), true)
                .output(EmiStack.of(PSItems.WINE_GRAPES))
                .build());

        SimpleFluid.all().forEach(fluid -> {
            if (!fluid.isEmpty()) {
                var contents = DrawingFluidEmiRecipe.Contents.of(fluid);
                registry.addRecipe(new DrawingFluidEmiRecipe(BARREL_CATEGORY, fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(DISTILLERY_CATEGORY, fluid, contents));
                registry.addRecipe(new DrawingFluidEmiRecipe(FLASK_CATEGORY, fluid, contents));

                if (fluid instanceof Processable p) {
                    FluidProcessingEmiRecipe.createRecipesFor(MATURING_CATEGORY, ProcessType.MATURE, p, fluid, registry::addRecipe);
                    FluidProcessingEmiRecipe.createRecipesFor(DISTILLING_CATEGORY, ProcessType.DISTILL, p, fluid, registry::addRecipe);
                    FluidProcessingEmiRecipe.createRecipesFor(FERMENTING_CATEGORY, ProcessType.FERMENT, p, fluid, registry::addRecipe);
                }
            }
        });

        Stream.of(
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE, PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        ).forEach(item -> registry.setDefaultComparison(item, comparison -> COMPARE_FLUID));
    }

}
