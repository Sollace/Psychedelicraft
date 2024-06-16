package ivorius.psychedelicraft.compat.emi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.EmiShapedRecipe;
import ivorius.psychedelicraft.recipe.FillRecepticalRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Deprecated
class FluidCraftingEmiRecipe extends EmiCraftingRecipe implements PSRecipe {
    private final EmiIngredient output;
    private final List<EmiStack> outputs;

    public FluidCraftingEmiRecipe(Identifier id, FillRecepticalRecipe recipe) {
        this(recipe.getIngredients().stream().map(EmiIngredient::of).toList(), id, recipe);
    }

    public FluidCraftingEmiRecipe(List<EmiIngredient> input, Identifier id, FillRecepticalRecipe recipe) {
        super(input, EmiStack.EMPTY, id, true);
        EmiShapedRecipe.setRemainders(input, recipe);
        input.get(0).getEmiStacks().forEach(stack -> stack.setRemainder(EmiStack.EMPTY));
        output = RecipeUtil.mergeReceptical(recipe.getIngredients().get(0), Optional.of(recipe.getOutputFluid()));
        outputs = Stream.concat(output.getEmiStacks().stream(), Stream.of(RecipeUtil.createFluidIngredient(recipe.getOutputFluid()))).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
        if (shapeless) {
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
        }
        int sOff = 0;
        if (!shapeless) {
            if (canFit(1, 3)) {
                sOff -= 1;
            }
            if (canFit(3, 1)) {
                sOff -= 3;
            }
        }
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            if (s >= 0 && s < input.size()) {
                widgets.addSlot(input.get(s), i % 3 * 18, i / 3 * 18);
            } else {
                widgets.addSlot(EmiStack.of(ItemStack.EMPTY), i % 3 * 18, i / 3 * 18);
            }
        }
        widgets.addSlot(output, 92, 14).large(true).recipeContext(this);
    }
}
