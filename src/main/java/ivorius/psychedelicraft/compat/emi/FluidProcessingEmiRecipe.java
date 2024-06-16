package ivorius.psychedelicraft.compat.emi;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Deprecated
class FluidProcessingEmiRecipe implements EmiRecipe, PSRecipe {

    private final Identifier id;
    private final EmiRecipeCategory category;

    private final int time;
    private final int change;

    private final EmiIngredient receptical;

    private final EmiIngredient inputFluid;
    private final EmiIngredient outputFluid;

    private final List<EmiStack> outputs;

    public FluidProcessingEmiRecipe(EmiRecipeCategory category,
            Processable.ProcessType type, SimpleFluid fluid,
            int counter,
            int time,
            int change,
            EmiIngredient receptical, Function<ItemFluids, ItemFluids> from, Function<ItemFluids, ItemFluids> to) {
        this.id = category.getId().withPath(p -> "fluid_process/" + p + "/" + fluid.getId().getPath() + "/" + counter);
        this.category = category;

        this.time = time;
        this.change = change;
        this.receptical = receptical;

        var inputItems = receptical.getEmiStacks().stream().map(r -> ItemFluids.set(r.getItemStack().copy(), from.apply(fluid.getDefaultStack(12)))).toList();
        var outputItems = receptical.getEmiStacks().stream().map(r -> ItemFluids.set(r.getItemStack().copy(), to.apply(fluid.getDefaultStack(12)))).toList();

        inputFluid = EmiIngredient.of(inputItems.stream().map(ItemFluids::of).distinct().map(RecipeUtil::createFluidIngredient).toList());
        outputFluid = EmiIngredient.of(outputItems.stream().map(ItemFluids::of).distinct().map(RecipeUtil::createFluidIngredient).toList());

        this.outputs = Stream.concat(Stream.concat(
                outputFluid.getEmiStacks().stream(),
                outputItems.stream().map(EmiStack::of)),
                receptical.getEmiStacks().stream()
        ).toList();
    }

    public static Stream<EmiRecipe> createRecipesFor(EmiRecipeCategory category, Processable.ProcessType type, Processable processable, SimpleFluid tankContents) {
        var counter = new Object() {
            int value;
        };
        return processable
            .getProcessStages(type, (time, change, from, to) -> new FluidProcessingEmiRecipe(category, type, tankContents, counter.value += 1, time, change, EmiIngredient.of(PSTags.Items.DRINK_RECEPTICALS), from, to));
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
        return List.of(inputFluid);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 50, 5);
        widgets.addAnimatedTexture(EmiTexture.FULL_ARROW, 50, 5, time / 20, true, false, false)
                    .tooltipText(List.of(Text.translatable("emi.cooking.time", time / 20F)));
        widgets.addText(Text.literal(change + "x"), 45, 14, -1, true);
        widgets.addSlot(inputFluid, 19, 5);
        widgets.addSlot(outputFluid, 85, 5).recipeContext(this);
        widgets.addSlot(receptical, 105, 5);
    }
}
