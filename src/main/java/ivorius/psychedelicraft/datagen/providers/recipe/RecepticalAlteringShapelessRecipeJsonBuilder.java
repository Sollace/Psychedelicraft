package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.ChangeRecepticalRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class RecepticalAlteringShapelessRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final DefaultedList<Ingredient> inputs = DefaultedList.of();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    @Nullable
    private String group;

    public RecepticalAlteringShapelessRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static RecepticalAlteringShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new RecepticalAlteringShapelessRecipeJsonBuilder(category, output, 1);
    }

    public static RecepticalAlteringShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
        return new RecepticalAlteringShapelessRecipeJsonBuilder(category, output, count);
    }

    public RecepticalAlteringShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        return input(Ingredient.fromTag(tag));
    }

    public RecepticalAlteringShapelessRecipeJsonBuilder input(ItemConvertible input) {
        return input(input, 1);
    }

    public RecepticalAlteringShapelessRecipeJsonBuilder input(ItemConvertible input, int count) {
        return input(Ingredient.ofItems(input), count);
    }

    public RecepticalAlteringShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public RecepticalAlteringShapelessRecipeJsonBuilder input(Ingredient ingredient, int count) {
        for (int i = 0; i < count; i++) {
            inputs.add(ingredient);
        }
        return this;
    }

    @Override
    public RecepticalAlteringShapelessRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        advancementBuilder.put(name, criterion);
        return this;
    }

    @Override
    public RecepticalAlteringShapelessRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        validate(recipeId);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        advancementBuilder.forEach(builder::criterion);
        exporter.accept(RecipeJsonBuilderCompat.createProvider(recipeId, PSRecipes.CHANGE_RECEPTICAL, new ChangeRecepticalRecipe(
                Objects.requireNonNullElse(group, ""),
                getCraftingCategory(category),
                new ItemStack(output, count),
                inputs
            ), builder.build(recipeId.withPrefixedPath("recipes/" + category.getName() + "/"))));
    }

    private void validate(Identifier recipeId) {
        if (advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
