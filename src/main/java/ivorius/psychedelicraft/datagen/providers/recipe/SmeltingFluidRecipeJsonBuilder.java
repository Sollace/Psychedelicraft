package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.FluidModifyingResult;
import ivorius.psychedelicraft.recipe.OptionalFluidIngredient;
import ivorius.psychedelicraft.recipe.SmeltingFluidRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;

public class SmeltingFluidRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final CookingRecipeCategory cookingCategory;

    private final OptionalFluidIngredient input;
    private Item output = Items.AIR;
    private final float experience;
    private final int cookingTime;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private final Map<String, FluidModifyingResult.Modification> modifications = new HashMap<>();

    private SmeltingFluidRecipeJsonBuilder(RecipeCategory category, CookingRecipeCategory cookingCategory, OptionalFluidIngredient input, float experience, int cookingTime) {
        this.category = category;
        this.cookingCategory = cookingCategory;
        this.input = input;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    public static SmeltingFluidRecipeJsonBuilder create(OptionalFluidIngredient input, RecipeCategory category, float experience, int cookingTime) {
        return new SmeltingFluidRecipeJsonBuilder(category, CookingRecipeCategory.FOOD, input, experience, cookingTime);
    }

    @Override
    public SmeltingFluidRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public SmeltingFluidRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public SmeltingFluidRecipeJsonBuilder output(ItemConvertible output) {
        this.output = output.asItem();
        return this;
    }

    public SmeltingFluidRecipeJsonBuilder modification(String attribute, FluidModifyingResult.Ops op, int value) {
        modifications.put(attribute, new FluidModifyingResult.Modification(value, op));
        return this;
    }

    @Override
    public Item getOutputItem() {
        throw new IllegalStateException("Recipe must provide an explicit name");
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        recipeKey = RegistryKey.of(recipeKey.getRegistryRef(), recipeKey.getValue().withSuffixedPath("_from_smelting"));
        validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
            .rewards(AdvancementRewards.Builder.recipe(recipeKey))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(recipeKey, new SmeltingFluidRecipe(
                Objects.requireNonNullElse(group, ""),
                cookingCategory,
                input,
                new FluidModifyingResult(modifications, output.getDefaultStack()),
                experience,
                cookingTime
        ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
        }
    }
}
