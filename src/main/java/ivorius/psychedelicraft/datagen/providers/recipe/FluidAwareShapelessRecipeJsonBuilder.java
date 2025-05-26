package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.FluidAwareShapelessRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class FluidAwareShapelessRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final DefaultedList<OptionalFluidIngredient> inputs = DefaultedList.of();
    private final Map<String, CriterionConditions> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private Ingredient destroyedContainer = Ingredient.empty();

    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

    public FluidAwareShapelessRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static FluidAwareShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new FluidAwareShapelessRecipeJsonBuilder(category, output, 1);
    }

    public static FluidAwareShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
        return new FluidAwareShapelessRecipeJsonBuilder(category, output, count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        return input(Ingredient.fromTag(tag));
    }

    public FluidAwareShapelessRecipeJsonBuilder input(ItemConvertible input) {
        return input(input, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(ItemConvertible input, int count) {
        return input(Ingredient.ofItems(input), count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid, TagKey<Item> receptical) {
        return input(fluid, receptical, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid, TagKey<Item> receptical, int count) {
        return input(OptionalFluidIngredient.of(fluid, Ingredient.fromTag(receptical)), count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid, ItemConvertible receptical) {
        return input(fluid, receptical, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid, ItemConvertible receptical, int count) {
        return input(OptionalFluidIngredient.of(fluid, Ingredient.ofItems(receptical)), count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid) {
        return input(fluid, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(FluidIngredient fluid, int count) {
        return input(OptionalFluidIngredient.of(fluid), count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(Ingredient ingredient, int count) {
        return input(OptionalFluidIngredient.of(ingredient), count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(OptionalFluidIngredient ingredient) {
        return input(ingredient, 1);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(OptionalFluidIngredient ingredient, int count) {
        for (int i = 0; i < count; i++) {
            inputs.add(ingredient);
        }
        return this;
    }

    public FluidAwareShapelessRecipeJsonBuilder discard(ItemConvertible item) {
        destroyedContainer = Ingredient.ofItems(item);
        return this;
    }

    @Override
    public FluidAwareShapelessRecipeJsonBuilder criterion(String name, CriterionConditions criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public FluidAwareShapelessRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        validate(recipeId);
        Advancement.Builder builder = advancementBuilder
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(
                PSRecipes.CRAFTING_SHAPELESS_FLUID,
                new FluidAwareShapelessRecipe(
                        recipeId,
                        Objects.requireNonNullElse(group, ""),
                        getCraftingCategory(category),
                        new ItemStack(output, count),
                        inputs,
                        destroyedContainer
                    ),
                builder, recipeId.withPrefixedPath("recipes/" + category.getName() + "/")
        ));
    }

    private void validate(Identifier recipeId) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
