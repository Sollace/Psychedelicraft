package ivorius.psychedelicraft.compat.tia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack.TlaItemStack;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.util.Identifier;

class DrawingFluidEmiRecipe implements PSRecipe {

    private final Identifier id;
    private final RecipeCategory category;

    private final Contents contents;

    private final Identifier background;

    private final TextureConfig arrow;
    private final TextureConfig arrowFill;

    private final int capacity;

    public static BiConsumer<RecipeCategory, PluginContext> generate(int capacity) {
        return (category, context) -> context.addGenerator(client -> SimpleFluid.REGISTRY.stream()
                .filter(f -> !f.isEmpty())
                .map(fluid -> (TlaRecipe)new DrawingFluidEmiRecipe(category, fluid, capacity))
                .toList());
    }

    public DrawingFluidEmiRecipe(RecipeCategory category, SimpleFluid fluid, int capacity) {
        this.id = category.getId().withPath(p -> "fluid_withdrawl/" + p + "/" + fluid.getId().getPath());
        this.category = category;
        this.background = category.getId().withPath(p -> "textures/gui/" + p + ".png");
        this.arrow = TextureConfig.builder().size(120, 64).texture(background).uv(15, 15).build();
        this.arrowFill = TextureConfig.builder().size(39, 64).texture(background).uv(191, 20).build();
        this.contents = Contents.of(fluid);
        this.capacity = capacity;
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
        return List.of();
    }

    @Override
    public List<TlaStack> getOutputs() {
        return contents.outputs();
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return contents.recepticals();
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        FluidBoxWidget.create(contents.fluid(), capacity, 50, 2, 40, 50, widgets);
        widgets.addTexture(arrow, 0, 0);
        widgets.addAnimatedTexture(arrowFill, 68, 47, 2000, true, true, true);
        widgets.addSlot(contents.output(), 107, 45).markOutput();
    }

    record Contents(List<TlaIngredient> recepticals, List<TlaStack> outputs, List<ItemFluids> fluid, TlaIngredient output) {
        static Contents of(SimpleFluid tankContents) {
            List<TlaIngredient> tanks = new ArrayList<>();
            List<ItemFluids> fluids = new ArrayList<>();
            Set<TlaIngredient> ingredients = new HashSet<>();
            List<TlaStack> outputs = new ArrayList<>();
            TlaIngredient.ofItemTag(tankContents.getPreferredContainerTag()).getStacks().forEach(stack -> {
               tankContents.getDefaultStacks(((TlaItemStack)stack).getItemVariant().toStack(), filled -> {
                   outputs.add(TlaStack.of(filled));
                   var fluid = ItemFluids.of(filled);
                   fluids.add(fluid);
                   tanks.add(TlaIngredient.ofStacks(RecipeUtil.toTlaStack(fluid)));
                   ingredients.add(TlaIngredient.ofStacks(stack));
               });
            });
            return new Contents(List.of(TlaIngredient.join(ingredients)), outputs, fluids, TlaIngredient.ofStacks(outputs));
        }
    }
}
