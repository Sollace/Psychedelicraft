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
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;

public class MixingRecipeJsonBuilder implements FluidRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> lookup;
    private final RecipeCategory category;
    private final ItemFluids output;
    private final DefaultedList<Ingredient> inputs = DefaultedList.of();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    @Nullable
    private String group;

    private Ingredient receptical;

    public MixingRecipeJsonBuilder(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemFluids output) {
        this.lookup = lookup;
        this.category = category;
        this.output = output;
        receptical(PSTags.Items.DRINK_RECEPTICALS);
    }

    public static MixingRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemFluids output) {
        return new MixingRecipeJsonBuilder(lookup, category, output);
    }

    public static MixingRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, SimpleFluid output, int amount) {
        return new MixingRecipeJsonBuilder(lookup, category, output.getDefaultStack(amount));
    }

    public MixingRecipeJsonBuilder input(TagKey<Item> tag) {
        return input(Ingredient.fromTag(lookup.getOrThrow(tag)));
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
        this.receptical = new OptionalFluidIngredient(Optional.of(FluidIngredient.builder().fluid(fluid).build()), Optional.of(Ingredient.fromTag(lookup.getOrThrow(tag)))).toVanilla();
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
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        recipeKey = RegistryKey.of(recipeKey.getRegistryRef(), recipeKey.getValue().withSuffixedPath("_from_mixing"));
        validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
            .rewards(AdvancementRewards.Builder.recipe(recipeKey))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        advancementBuilder.forEach(builder::criterion);
        exporter.accept(recipeKey, new MixingRecipe(
                Objects.requireNonNullElse(group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(category),
                output,
                receptical,
                inputs
            ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
        }
    }
}
