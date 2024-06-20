package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

class FluidProcessingEmiRecipe implements PSRecipe {
    private final Identifier id;
    private final RecipeCategory category;

    private final int time;
    private final int change;

    private final TlaIngredient receptical;

    private final TlaIngredient input;
    private final TlaIngredient output;

    private final List<TlaStack> outputs;

    public static BiConsumer<RecipeCategory, PluginContext> generate(Processable.ProcessType type, int capacity) {
        return (category, context) -> context.addGenerator(client -> SimpleFluid.REGISTRY.stream()
                .filter(fluid -> fluid instanceof Processable)
                .flatMap(fluid -> {
            var counter = new Object() { int value; };
            return ((Processable)fluid).getProcessStages(type, (time, change, from, to) -> {
                return (TlaRecipe)new FluidProcessingEmiRecipe(category, type, capacity, fluid, counter.value += 1, time, change, from, to);
            });
        }).toList());
    }

    private FluidProcessingEmiRecipe(RecipeCategory category,
            Processable.ProcessType type, int capacity, SimpleFluid fluid,
            int counter,
            int time,
            int change,
            Function<ItemFluids, ItemFluids> from, Function<ItemFluids, ItemFluids> to) {
        this.id = category.getId().withPath(p -> "fluid_process/" + p + "/" + fluid.getId().getPath() + "/" + counter);
        this.category = category;

        this.time = time;
        this.change = change;
        this.receptical = category.stations();

        ItemFluids def = fluid.getDefaultStack();
        ItemFluids fromFluid = from.apply(def);
        ItemFluids toFluid = to.apply(def);

        input = RecipeUtil.toIngredient(fromFluid);
        output = RecipeUtil.toIngredient(toFluid);
        outputs = List.of(RecipeUtil.toTlaStack(toFluid));
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<TlaStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of(receptical);
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        widgets.addArrow(50, 5, false);
        widgets.addAnimatedArrow(50, 5, time / 20).addTooltip(Text.translatable("emi.cooking.time", time / 20F));
        widgets.addText(Text.literal(change + "x"), 45, 14, -1, true);
        widgets.addSlot(input, 19, 5).markInput();
        widgets.addSlot(output, 85, 5).markOutput();
        widgets.addSlot(receptical, 105, 5).markCatalyst();
    }
}
