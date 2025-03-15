package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import ivorius.psychedelicraft.recipe.BottleRecipe;
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
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

public class BottleRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> lookup;
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<String> pattern = Lists.<String>newArrayList();
    private final Map<Character, Ingredient> inputs = new LinkedHashMap<>();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public BottleRecipeJsonBuilder(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output, int count) {
        this.lookup = lookup;
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static BottleRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output) {
        return create(lookup, category, output, 1);
    }

    public static BottleRecipeJsonBuilder create(RegistryEntryLookup<Item> lookup, RecipeCategory category, ItemConvertible output, int count) {
        return new BottleRecipeJsonBuilder(lookup, category, output, count);
    }

    public BottleRecipeJsonBuilder input(Character c, TagKey<Item> tag) {
        return input(c, Ingredient.fromTag(lookup.getOrThrow(tag)));
    }

    public BottleRecipeJsonBuilder input(Character c, ItemConvertible itemProvider) {
        return input(c, Ingredient.ofItems(itemProvider));
    }

    public BottleRecipeJsonBuilder input(Character c, Ingredient ingredient) {
        if (inputs.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        }
        if (c == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }

        inputs.put(c, ingredient);
        return this;
    }

    public BottleRecipeJsonBuilder pattern(String patternStr) {
        if (!pattern.isEmpty() && patternStr.length() != pattern.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }

        pattern.add(patternStr);
        return this;
    }

    @Override
    public BottleRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public BottleRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public BottleRecipeJsonBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        RawShapedRecipe rawShapedRecipe = validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
            .rewards(AdvancementRewards.Builder.recipe(recipeKey))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(recipeKey, new BottleRecipe(
            Objects.requireNonNullElse(group, ""),
            CraftingRecipeJsonBuilder.toCraftingCategory(category),
            rawShapedRecipe,
            new ItemStack(output, count),
            this.showNotification
        ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    private RawShapedRecipe validate(RegistryKey<Recipe<?>> recipeKey) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
        }
        return RawShapedRecipe.create(inputs, pattern);
    }
}
