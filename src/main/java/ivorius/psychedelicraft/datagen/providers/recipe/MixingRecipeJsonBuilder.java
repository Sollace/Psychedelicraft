package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.MixingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class MixingRecipeJsonBuilder extends RecipeJsonBuilder implements FluidRecipeJsonBuilder {
    private final RecipeCategory category;
    private final ItemFluids output;
    private final DefaultedList<Ingredient> inputs = DefaultedList.of();
    private final Map<String, CriterionConditions> criteria = new LinkedHashMap<>();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

    @Nullable
    private String group;

    private Ingredient receptical = Ingredient.fromTag(PSTags.Items.DRINK_RECEPTICALS);

    public MixingRecipeJsonBuilder(RecipeCategory category, ItemFluids output) {
        this.category = category;
        this.output = output;
    }

    public static MixingRecipeJsonBuilder create(RecipeCategory category, ItemFluids output) {
        return new MixingRecipeJsonBuilder(category, output);
    }

    public static MixingRecipeJsonBuilder create(RecipeCategory category, SimpleFluid output, int amount) {
        return new MixingRecipeJsonBuilder(category, output.getDefaultStack(amount));
    }

    public MixingRecipeJsonBuilder input(TagKey<Item> tag) {
        return input(Ingredient.fromTag(tag));
    }

    public MixingRecipeJsonBuilder input(ItemConvertible input) {
        return input(input, 1);
    }

    public MixingRecipeJsonBuilder input(ItemConvertible input, int count) {
        return input(Ingredient.ofItems(input), count);
    }

    public MixingRecipeJsonBuilder input(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public MixingRecipeJsonBuilder input(Ingredient ingredient, int count) {
        for (int i = 0; i < count; i++) {
            inputs.add(ingredient);
        }
        return this;
    }

    public MixingRecipeJsonBuilder receptical(TagKey<Item> tag) {
        this.receptical = Ingredient.fromTag(tag);
        return this;
    }

    public MixingRecipeJsonBuilder receptical(ItemConvertible receptical) {
        this.receptical = Ingredient.ofItems(receptical);
        return this;
    }

    @Override
    public MixingRecipeJsonBuilder criterion(String name, CriterionConditions criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public MixingRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ItemFluids getOutputFluids() {
        return output;
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        recipeId = recipeId.withSuffixedPath("_from_mixing");
        validate(recipeId);
        Advancement.Builder builder = advancementBuilder
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(PSRecipes.FILL_RECEPTICAL, new MixingRecipe(
                recipeId,
                Objects.requireNonNullElse(group, ""),
                getCraftingCategory(category),
                output,
                receptical,
                inputs
            ), builder, recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
