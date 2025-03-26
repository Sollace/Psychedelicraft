package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.FluidAwareShapelessRecipe;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;

public class FluidAwareShapelessRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> lookup;
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final DefaultedList<OptionalFluidIngredient> inputs = DefaultedList.of();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    @Nullable
    private String group;
    private Optional<Ingredient> destroyedContainer = Optional.empty();

    public FluidAwareShapelessRecipeJsonBuilder(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output, int count) {
        this.lookup = lookup;
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static FluidAwareShapelessRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output) {
        return new FluidAwareShapelessRecipeJsonBuilder(lookup, category, output, 1);
    }

    public static FluidAwareShapelessRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output, int count) {
        return new FluidAwareShapelessRecipeJsonBuilder(lookup, category, output, count);
    }

    public FluidAwareShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        return input(Ingredient.fromTag(lookup.getOrThrow(tag)));
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
        return input(OptionalFluidIngredient.of(fluid, Ingredient.fromTag(lookup.getOrThrow(receptical))), count);
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
        destroyedContainer = Optional.of(Ingredient.ofItems(item));
        return this;
    }

    @Override
    public FluidAwareShapelessRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.advancementBuilder.put(name, criterion);
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
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
            .rewards(AdvancementRewards.Builder.recipe(recipeKey))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        advancementBuilder.forEach(builder::criterion);
        exporter.accept(recipeKey, new FluidAwareShapelessRecipe(
                Objects.requireNonNullElse(group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(category),
                new ItemStack(output, count),
                inputs,
                destroyedContainer
            ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
        }
    }
}
