package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.MixingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
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
        return receptical(tag, Fluids.WATER);
    }

    public MixingRecipeJsonBuilder receptical(ItemConvertible receptical) {
        return receptical(receptical, Fluids.WATER);
    }

    public MixingRecipeJsonBuilder receptical(TagKey<Item> tag, Fluid fluid) {
        this.receptical = new OptionalFluidIngredient(Optional.of(FluidIngredient.builder().fluid(fluid).build()), Optional.of(Ingredient.fromTag(tag))).toVanilla();
        return this;
    }

    public MixingRecipeJsonBuilder receptical(ItemConvertible receptical, Fluid fluid) {
        this.receptical = new OptionalFluidIngredient(Optional.of(FluidIngredient.builder().fluid(fluid).build()), Optional.of(Ingredient.ofItems(receptical))).toVanilla();
        return this;
    }

    @Override
    public MixingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        advancementBuilder.put(name, criterion);
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
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        recipeId = recipeId.withSuffixedPath("_from_mixing");
        validate(recipeId);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        advancementBuilder.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(recipeId, PSRecipes.FILL_RECEPTICAL, new MixingRecipe(
                Objects.requireNonNullElse(group, ""),
                getCraftingCategory(category),
                output,
                receptical,
                inputs
            ), builder.build(recipeId.withPrefixedPath("recipes/" + category.getName() + "/"))));
    }

    private void validate(Identifier recipeId) {
        if (advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
