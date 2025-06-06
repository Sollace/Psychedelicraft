package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.recipe.HardeningRecipe;
import ivorius.psychedelicraft.recipe.ImpuritiesPredicate;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class HardeningRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
	private final RecipeCategory category;

	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

	@Nullable
	private String group;

	private FluidIngredient baseFluid = FluidIngredient.EMPTY;
	private final List<FluidIngredient> solutions = new ArrayList<>();

	private final Item output;

	private final IntProvider amount;

	private ImpuritiesPredicate impurities = ImpuritiesPredicate.EMPTY;

	private int stewTime = 20;

	private HardeningRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, IntProvider amount) {
		this.category = category;
		this.output = output.asItem();
		this.amount = amount;
	}

    public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new HardeningRecipeJsonBuilder(category, output, UniformIntProvider.create(3, 6));
    }

    public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, IntProvider amount) {
        return new HardeningRecipeJsonBuilder(category, output, amount);
    }

	@Override
    public HardeningRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
		this.criteria.put(name, criterion);
		return this;
	}

	public HardeningRecipeJsonBuilder base(FluidIngredient.Builder builder) {
	    baseFluid = builder.build();
	    return this;
	}

    public HardeningRecipeJsonBuilder solution(FluidIngredient.Builder builder) {
        solutions.add(builder.build());
        return this;
    }

    public HardeningRecipeJsonBuilder impurity(ImpuritiesPredicate.Builder impurities) {
        this.impurities = impurities.build();
        return this;
    }

	@Override
    public HardeningRecipeJsonBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	public HardeningRecipeJsonBuilder stewTime(int stewTime) {
	    this.stewTime = stewTime;
	    return this;
	}

    @Override
    public Item getOutputItem() {
        return output;
    }

	@Override
	public void offerTo(RecipeExporter exporter, Identifier recipeId) {
	    recipeId = recipeId.withSuffixedPath("_from_tray");
		validate(recipeId);
		Advancement.Builder builder = exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
			.rewards(AdvancementRewards.Builder.recipe(recipeId))
			.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		criteria.forEach(builder::criterion);
		ItemStack result = new ItemStack(output);
		exporter.accept(recipeId, new HardeningRecipe(
	            Objects.requireNonNullElse(group, ""),
	            baseFluid,
	            solutions,
	            impurities,
	            result,
	            amount,
	            stewTime
        ), builder.build(recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
	}

	private void validate(Identifier recipeId) {
		if (criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		}
		Objects.requireNonNull(output, "Mashing recipe has no output");
	}
}