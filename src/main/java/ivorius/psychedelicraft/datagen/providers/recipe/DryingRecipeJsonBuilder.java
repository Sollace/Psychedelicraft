package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.DryingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class DryingRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
	private final RecipeCategory category;
	private final Item output;
	private final int outputCount;
	private final Ingredient input;
	private final float experience;
	private final float cookingTime;

	private final Map<String, CriterionConditions> criteria = new LinkedHashMap<>();

	private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

	@Nullable
	private String group;

	private DryingRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int outputCount, Ingredient input, float experience, float cookingTime) {
		this.category = category;
		this.output = output.asItem();
		this.outputCount = outputCount;
		this.input = input;
		this.experience = experience;
		this.cookingTime = cookingTime;
	}

	public static DryingRecipeJsonBuilder create(Ingredient input, RecipeCategory category, ItemConvertible output, int outputCount, float experience, float cookingTime) {
		return new DryingRecipeJsonBuilder(category, output, outputCount, input, experience, cookingTime);
	}

	@Override
    public DryingRecipeJsonBuilder criterion(String name, CriterionConditions criterion) {
		this.criteria.put(name, criterion);
		return this;
	}

	@Override
    public DryingRecipeJsonBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	@Override
	public Item getOutputItem() {
		return output;
	}

	@Override
	public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
	    recipeId = recipeId.withSuffixedPath("_from_drying");
		validate(recipeId);
		Advancement.Builder builder = advancementBuilder
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
			.rewards(AdvancementRewards.Builder.recipe(recipeId))
			.criteriaMerger(CriterionMerger.OR);
		criteria.forEach(builder::criterion);
		exporter.accept(RecipeJsonBuilderCompat.createProvider(PSRecipes.DRYING, new DryingRecipe(
		        recipeId,
                Objects.requireNonNullElse(group, ""),
                input, new ItemStack(output, outputCount),
                experience, cookingTime), builder, recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
	}

	private void validate(Identifier recipeId) {
		if (criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		}
	}
}