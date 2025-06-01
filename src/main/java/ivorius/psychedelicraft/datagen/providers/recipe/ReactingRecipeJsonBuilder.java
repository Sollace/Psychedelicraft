package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.ReactingRecipe;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ReactingRecipeJsonBuilder extends RecipeJsonBuilder implements FluidRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Map<String, CriterionConditions> criterions = new LinkedHashMap<>();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

    @Nullable
    private String group;

    private ItemFluids output;
    @Nullable
    private Impurities.Impurity impurity;
    private Item byProduct = Items.AIR;

    private int stewTime;

    private final DefaultedList<FluidIngredient> inputFluids = DefaultedList.of();
    private final DefaultedList<Ingredient> inputItems = DefaultedList.of();

    private ReactingRecipeJsonBuilder(RecipeCategory category, ItemFluids output) {
        this.category = category;
        this.output = output;
    }

    public static ReactingRecipeJsonBuilder create(RecipeCategory category, ItemFluids output) {
        return new ReactingRecipeJsonBuilder(category, output);
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
        return input(Ingredient.fromTag(item));
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
    public ReactingRecipeJsonBuilder criterion(String name, CriterionConditions criterion) {
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
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        recipeId = recipeId.withSuffixedPath("_from_reacting");
        validate(recipeId);
        Advancement.Builder builder = advancementBuilder
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR);
        criterions.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(PSRecipes.REACTING, new ReactingRecipe(
                recipeId,
                Objects.requireNonNullElse(group, ""),
                getCraftingCategory(category),
                new ReactingRecipe.Result(output, new ItemStack(byProduct), Optional.ofNullable(impurity)),
                new ReactingRecipe.Ingredients(inputFluids, inputItems),
                stewTime
            ), builder, recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (criterions.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
