package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class MashingRecipeJsonBuilder implements FluidRecipeJsonBuilder {
    static final ItemFluids.Predicate WATER_PREDICATE = ItemFluids.Predicate.builder()
            .fluid(SimpleFluid.of(Fluids.WATER))
            .amount(IntRange.atLeast(FluidVolumes.VAT / 4))
            .build();
	private final RecipeCategory category;

	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

	@Nullable
	private String group;

	private ItemFluids.Predicate baseFluid = WATER_PREDICATE;

	private final ItemFluids output;

	private int stewTime;

	private final List<MashingRecipe.Ingredients.Entry> ingredients = new ArrayList<>();

	private MashingRecipeJsonBuilder(RecipeCategory category, ItemFluids output) {
		this.category = category;
		this.output = output;
	}

	public static MashingRecipeJsonBuilder create(RecipeCategory category, ItemFluids output) {
		return new MashingRecipeJsonBuilder(category, output);
	}

	@Override
    public MashingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
		this.criteria.put(name, criterion);
		return this;
	}

	public MashingRecipeJsonBuilder base(ItemFluids.Predicate.Builder builder) {
	    baseFluid = builder.build();
	    return this;
	}

	public MashingRecipeJsonBuilder input(TagKey<Item> tag, int count) {
        return input(Ingredient.fromTag(tag), count);
    }

	public MashingRecipeJsonBuilder input(ItemConvertible input, int count) {
	    return input(Ingredient.ofItems(input), count);
	}

    public MashingRecipeJsonBuilder input(ItemConvertible input) {
        return input(input, 1);
    }

    public MashingRecipeJsonBuilder input(Ingredient input) {
        return input(input, 1);
    }

    public MashingRecipeJsonBuilder input(Ingredient input, int count) {
        ingredients.add(new MashingRecipe.Ingredients.Entry(input, count));
        return this;
    }

	@Override
    public MashingRecipeJsonBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	public MashingRecipeJsonBuilder stewTime(int stewTime) {
	    this.stewTime = stewTime;
	    return this;
	}

    @Override
    public ItemFluids getOutputFluids() {
        return output;
    }

	@Override
	public void offerTo(RecipeExporter exporter, Identifier recipeId) {
	    recipeId = recipeId.withSuffixedPath("_from_mashing");
		validate(recipeId);
		Advancement.Builder builder = exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
			.rewards(AdvancementRewards.Builder.recipe(recipeId))
			.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		criteria.forEach(builder::criterion);
		exporter.accept(recipeId, new MashingRecipe(
	            Objects.requireNonNullElse(group, ""),
	            CraftingRecipeJsonBuilder.toCraftingCategory(category),
	            baseFluid,
	            output,
	            new MashingRecipe.Ingredients(DefaultedList.copyOf(
                        MashingRecipe.Ingredients.Entry.EMPTY,
                        ingredients.toArray(MashingRecipe.Ingredients.Entry[]::new)
                )),
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