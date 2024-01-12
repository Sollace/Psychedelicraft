package ivorius.psychedelicraft.compat.emi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.recipe.SmeltingFluidRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SmeltingFluidEmiRecipe implements EmiRecipe {
    private final Identifier id;
    private final SmeltingFluidRecipe recipe;
    private final EmiIngredient input;
    private final List<EmiStack> output;

    public SmeltingFluidEmiRecipe(Identifier id, SmeltingFluidRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        input = RecipeUtil.mergeReceptical(recipe.getIngredients().get(0), Optional.of(recipe.getFluid()));
        output = input.getEmiStacks().stream().map(stack -> recipe.getResult().applyTo(stack.getItemStack())).map(EmiStack::of).toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.SMELTING;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 82;
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addFillingArrow(24, 5, 50 * recipe.getCookingTime())
            .tooltipText(List.of(Text.translatable("emi.cooking.time", recipe.getCookingTime() / 20f)));

        widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24);
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 24, 4000 / recipe.getCookingTime(), false, true, true);

        widgets.addText(EmiPort.ordered(EmiPort.translatable("emi.cooking.experience", recipe.getExperience())), 26, 28, -1, true);
        widgets.addSlot(input, 0, 4);
        widgets.addSlot(output.get(0), 56, 0).large(true).recipeContext(this);
    }
}