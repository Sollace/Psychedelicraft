package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.recipe.HardeningRecipe;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
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
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class HardeningRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
	private final RecipeCategory category;

	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

	@Nullable
	private String group;

	private FluidIngredient baseFluid = FluidIngredient.EMPTY;
	private final List<FluidIngredient> impurities = new ArrayList<>();

	private final Item output;

	private final IntProvider amount;

	private final Set<Impurities.Impurity> cuts;

	private int stewTime = 20;

	private HardeningRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, IntProvider amount, Set<Impurities.Impurity> cuts) {
		this.category = category;
		this.output = output.asItem();
		this.amount = amount;
		this.cuts = cuts;
	}

    public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new HardeningRecipeJsonBuilder(category, output, UniformIntProvider.create(3, 6), Set.of());
    }

	public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, IntProvider amount) {
		return new HardeningRecipeJsonBuilder(category, output, amount, Set.of());
	}

    public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, Set<Impurities.Impurity> impurities) {
        return new HardeningRecipeJsonBuilder(category, output, UniformIntProvider.create(3, 6), impurities);
    }

    public static HardeningRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, IntProvider amount, Set<Impurities.Impurity> impurities) {
        return new HardeningRecipeJsonBuilder(category, output, amount, impurities);
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

    public HardeningRecipeJsonBuilder impurity(FluidIngredient.Builder builder) {
        impurities.add(builder.build());
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
	public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
	    recipeKey = RegistryKey.of(recipeKey.getRegistryRef(), recipeKey.getValue().withSuffixedPath("_from_tray"));
		validate(recipeKey);
		Advancement.Builder builder = exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
			.rewards(AdvancementRewards.Builder.recipe(recipeKey))
			.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		criteria.forEach(builder::criterion);
		ItemStack result = new ItemStack(output);
		exporter.accept(recipeKey, new HardeningRecipe(
	            Objects.requireNonNullElse(group, ""),
	            baseFluid,
	            impurities,
	            new Impurities(cuts),
	            result,
	            amount,
	            stewTime
        ), builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
	}

	private void validate(RegistryKey<Recipe<?>> recipeKey) {
		if (criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + recipeKey);
		}
		Objects.requireNonNull(output, "Mashing recipe has no output");
	}
}