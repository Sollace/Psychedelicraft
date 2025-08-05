package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.BunsenBurnerRecipe;
import ivorius.psychedelicraft.recipe.ReactingRecipe;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;

public class ReactingRecipeJsonBuilder implements FluidRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> lookup;
    private final BunsenBurnerRecipe.ReactionType category;
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;

    private ItemFluids output;
    @Nullable
    private Impurities.Impurity impurity;
    private Item byProduct = Items.AIR;

    private int stewTime;

    private final DefaultedList<FluidIngredient> inputFluids = DefaultedList.of();
    private final DefaultedList<Ingredient> inputItems = DefaultedList.of();

    private ReactingRecipeJsonBuilder(RegistryEntryLookup<Item> lookup, BunsenBurnerRecipe.ReactionType category, ItemFluids output) {
        this.lookup = lookup;
        this.category = category;
        this.output = output;
    }

    public static ReactingRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, BunsenBurnerRecipe.ReactionType category, ItemFluids output) {
        return new ReactingRecipeJsonBuilder(lookup, category, output);
    }

    public ReactingRecipeJsonBuilder byProduct(ItemConvertible item) {
        this.byProduct = item.asItem();
        return this;
    }

    public ReactingRecipeJsonBuilder impurity(Impurities.Impurity impurity) {
        this.impurity = impurity;
        return this;
    }

    public ReactingRecipeJsonBuilder input(SimpleFluid fluid) {
        return input(FluidIngredient.builder().fluid(fluid).build());
    }

    public ReactingRecipeJsonBuilder input(FluidIngredient fluid) {
        inputFluids.add(fluid);
        return this;
    }

    public ReactingRecipeJsonBuilder input(TagKey<Item> item) {
        return input(Ingredient.fromTag(lookup.getOrThrow(item)));
    }

    public ReactingRecipeJsonBuilder input(ItemConvertible item) {
        return input(Ingredient.ofItems(item));
    }

    public ReactingRecipeJsonBuilder input(Ingredient ingredient) {
        inputItems.add(ingredient);
        return this;
    }

    public ReactingRecipeJsonBuilder stewTime(int stewTime) {
        this.stewTime = stewTime;
        return this;
    }

    @Override
    public ReactingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        criterions.put(name, criterion);
        return this;
    }

    @Override
    public ReactingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public ItemFluids getOutputFluids() {
        return output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        recipeKey = RegistryKey.of(recipeKey.getRegistryRef(), recipeKey.getValue().withSuffixedPath("_from_reacting"));
        validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
            .rewards(AdvancementRewards.Builder.recipe(recipeKey))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criterions.forEach(builder::criterion);
        exporter.accept(recipeKey, new ReactingRecipe(
                category,
                Objects.requireNonNullElse(group, ""),
                CraftingRecipeCategory.MISC,
                new ReactingRecipe.Result(output, new ItemStack(byProduct), Optional.ofNullable(impurity)),
                new ReactingRecipe.Ingredients(inputFluids, inputItems),
                stewTime
            ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + RecipeCategory.BREWING.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (criterions.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
        }
    }
}
