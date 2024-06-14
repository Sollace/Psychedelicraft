package ivorius.psychedelicraft.compat.emi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.util.Identifier;

public class DrawingFluidEmiRecipe implements EmiRecipe, PSRecipe {

    private final Identifier id;
    private final EmiRecipeCategory category;

    private final Contents contents;

    private final Identifier background;

    public DrawingFluidEmiRecipe(EmiRecipeCategory category, SimpleFluid tankContents, Contents contents) {
        this.id = category.getId().withPath(p -> "fluid_withdrawl/" + p + "/" + tankContents.getId().getPath());
        this.category = category;
        this.background = category.getId().withPath(p -> "textures/gui/" + p + ".png");
        this.contents = contents;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return contents.inputs();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return contents.outputs();
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(background, 0, 0, 120, 64, 15, 15);

        //widgets.addAnimatedTexture(background, 5, 23, 39, 14, 177, 0, 2000, true, true, true);
        widgets.addAnimatedTexture(background, 68, 47, 39, 64, 191, 20, 2000, true, true, true);

        widgets.addSlot(contents.output(), 107, 45).recipeContext(this);
        widgets.addTank(contents.tank(), 62, 22, 19, 19, FluidVolumes.VAT / 12)
            .customBackground(background, 0, 0, 64, 64)
            .catalyst(true);
    }

    record Contents(List<EmiIngredient> inputs, List<EmiStack> outputs, EmiIngredient tank, EmiIngredient output) {
        static Contents of(SimpleFluid tankContents) {
            List<EmiIngredient> tanks = new ArrayList<>();
            Set<EmiIngredient> ingredients = new HashSet<>();
            List<EmiStack> outputs = new ArrayList<>();
            EmiIngredient.of(PSTags.Items.DRINK_RECEPTICALS).getEmiStacks().forEach(stack -> {
               tankContents.getDefaultStacks(stack.getItemStack(), filled -> {
                   outputs.add(EmiStack.of(filled));
                   var fluids = ItemFluids.of(filled);
                   tanks.add(RecipeUtil.createFluidIngredient(fluids));
                   ingredients.add(stack);
               });
            });
            var tank = new ListEmiIngredient(tanks, FluidVolumes.VAT / 12);
            var inputs = List.of(EmiIngredient.of(new ArrayList<>(ingredients)));
            return new Contents(inputs, outputs, tank, EmiIngredient.of(outputs));
        }
    }
}
