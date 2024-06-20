package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.CategoryIcon;
import io.github.mattidragon.tlaapi.api.recipe.TlaCategory;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

record RecipeCategory(Identifier id, CategoryIcon icon, TlaIngredient stations, int width, int height) implements TlaCategory {
    static final Map<RecipeCategory, @Nullable BiConsumer<RecipeCategory, PluginContext>> REGISTRY = new HashMap<>();

    public static final RecipeCategory DRYING_TABLE = register("drying_table", PSItems.DRYING_TABLE, 118, 74, (category, registry) -> registry.addRecipeGenerator(PSRecipes.DRYING_TYPE, DryingEmiRecipe::new));
    public static final RecipeCategory VAT = register("wooden_vat", PSItems.MASH_TUB, 130, 84, (category, registry) -> registry.addRecipeGenerator(PSRecipes.MASHING_TYPE, MashingEmiRecipe::new));
    public static final RecipeCategory BARREL = RecipeCategory.register("barrel", PSItems.OAK_BARREL, TlaIngredient.ofItemTag(PSTags.Items.BARRELS), 130, 70, DrawingFluidEmiRecipe.generate(FluidVolumes.BARREL));
    public static final RecipeCategory DISTILLERY = RecipeCategory.register("distillery", PSItems.DISTILLERY, 130, 70, DrawingFluidEmiRecipe.generate(FluidVolumes.FLASK));
    public static final RecipeCategory FLASK = RecipeCategory.register("flask", PSItems.FLASK, 130, 70, DrawingFluidEmiRecipe.generate(FluidVolumes.FLASK));

    public static final RecipeCategory FERMENTING = register("fermenting", PSItems.MASH_TUB, 130, 28, FluidProcessingEmiRecipe.generate(ProcessType.FERMENT, FluidVolumes.VAT));
    public static final RecipeCategory MATURING = RecipeCategory.register("maturing", PSItems.OAK_BARREL, TlaIngredient.ofItemTag(PSTags.Items.BARRELS), 130, 28, FluidProcessingEmiRecipe.generate(ProcessType.MATURE, FluidVolumes.BARREL));
    public static final RecipeCategory DISTILLING = RecipeCategory.register("distilling", PSItems.DISTILLERY, 130, 28, FluidProcessingEmiRecipe.generate(ProcessType.DISTILL, FluidVolumes.FLASK));

    public static final RecipeCategory WORLD_INTERACTION = register(new RecipeCategory(Identifier.of("emi", "world_interaction"), CategoryIcon.item(Items.GRASS_BLOCK), TlaStack.of(Items.GRASS_BLOCK).asIngredient(), 125, 18), WorldInteractionEmiRecipe::generate);

    static RecipeCategory register(String name, ItemConvertible station, int width, int height, @Nullable BiConsumer<RecipeCategory, PluginContext> recipeConstructor) {
        return register(name, station, TlaIngredient.ofStacks(TlaStack.of(station)), width, height, recipeConstructor);
    }

    static RecipeCategory register(String name, ItemConvertible icon, TlaIngredient station, int width, int height, @Nullable BiConsumer<RecipeCategory, PluginContext> recipeConstructor) {
        var id = Psychedelicraft.id(name);
        return register(new RecipeCategory(id, CategoryIcon.item(icon), station, width, height), recipeConstructor);
    }

    static RecipeCategory register(RecipeCategory category, @Nullable BiConsumer<RecipeCategory, PluginContext> recipeConstructor) {
        REGISTRY.put(category, recipeConstructor);
        return category;
    }


    static void bootstrap(PluginContext registry) {
        REGISTRY.forEach((category, recipeConstructor) -> {
            registry.addCategory(category);
            registry.addWorkstation(category, category.stations());
            try {
                if (recipeConstructor != null) {
                    recipeConstructor.accept(category, registry);
                }
            } catch (Throwable t) {
                Psychedelicraft.LOGGER.fatal("Error occured whilst registering recipes for category " + category.getId(), t);
            }
        });
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public int getDisplayWidth() {
        return width;
    }

    @Override
    public CategoryIcon getIcon() {
        return icon;
    }

    @Override
    public CategoryIcon getSimpleIcon() {
        return icon;
    }
}
