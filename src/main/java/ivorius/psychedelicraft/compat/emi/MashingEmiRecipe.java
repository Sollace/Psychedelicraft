package ivorius.psychedelicraft.compat.emi;

import java.util.List;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import net.minecraft.util.Identifier;

@Deprecated
class MashingEmiRecipe implements EmiRecipe, PSRecipe {

    private final Identifier id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    private final EmiStack tank;

    public MashingEmiRecipe(Identifier id, MashingRecipe recipe) {
        this.id = id;
        this.input = RecipeUtil.grouped(recipe.getIngredients().stream().map(EmiIngredient::of)).toList();
        this.output = RecipeUtil.createFluidIngredient(recipe.result()).getEmiStacks();
        this.tank = RecipeUtil.createFluidIngredient(recipe.baseFluid());
        tank.setAmount(FluidVolumes.VAT / 2 / 12);
        output.get(0).setAmount(FluidVolumes.VAT / 2 / 12);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return RecipeCategory.VAT.category();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 84;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Identifier background = Psychedelicraft.id("textures/gui/wooden_vat.png");

        int x = 7;
        int y = 27;

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60 + x, 21 + y);
        widgets.addAnimatedTexture(EmiTexture.FULL_ARROW, 60 + x, 21 + y, 5000, true, false, false);
        widgets.addTank(tank, 13 + x, 17 + y, 30, 30, FluidVolumes.VAT / 12)
            .drawBack(false);
        widgets.addTexture(background, x, 5 + y, 60, 44, 57, 15);

        x += 85;
        y += 15;

        widgets.addTank(output.get(0), 6 + x, 8 + y, 16, 16, FluidVolumes.VAT / 12)
            .drawBack(false)
            .recipeContext(this);
        widgets.addTexture(background, x, 5 + y, 30, 20, 28, 10, 30, 20, 128, 128);

        int sOff = 0;
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            if (s >= 0 && s < input.size()) {
                widgets.addSlot(input.get(s), (i % 4 * 16) + 7, (i / 4 * 16) + 7).drawBack(false);
            }
        }
    }
}
