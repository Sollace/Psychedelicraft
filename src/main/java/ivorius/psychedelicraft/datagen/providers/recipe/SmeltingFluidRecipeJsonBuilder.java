package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.FluidModifyingResult;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.SmeltingFluidRecipe;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class SmeltingFluidRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final CookingRecipeCategory cookingCategory;

    private final OptionalFluidIngredient input;
    private Item output = Items.AIR;
    private final float experience;
    private final int cookingTime;
    private final Map<String, CriterionConditions> criteria = new LinkedHashMap<>();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();
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
    public SmeltingFluidRecipeJsonBuilder criterion(String name, CriterionConditions criterion) {
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
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        recipeId = recipeId.withSuffixedPath("_from_smelting");
        validate(recipeId);
        Advancement.Builder builder = advancementBuilder
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(PSRecipes.SMELTING_RECEPTICAL, new SmeltingFluidRecipe(
                recipeId,
                Objects.requireNonNullElse(group, ""),
                cookingCategory,
                input,
                new FluidModifyingResult(modifications, output.getDefaultStack()),
                experience,
                cookingTime
        ), builder, recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
