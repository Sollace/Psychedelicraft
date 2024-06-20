package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.util.Identifier;

class DrawingFluidEmiRecipe implements PSRecipe {

    //private final Identifier id;
    private final RecipeCategory category;

    private final RecipeUtil.Contents contents;
    private final Identifier background;

    private final int capacity;

    public static BiConsumer<RecipeCategory, PluginContext> generate(int capacity) {
        return (category, context) -> context.addGenerator(client -> SimpleFluid.REGISTRY.stream()
                .filter(f -> !f.isEmpty())
                .flatMap(fluid -> TlaIngredient.ofItemTag(fluid.getPreferredContainerTag()).getStacks().stream()
                        .flatMap(receptical -> fluid.getDefaultStacks(capacity).map(stack -> RecipeUtil.Contents.of(receptical, stack))))
                .map(contents -> (TlaRecipe)new DrawingFluidEmiRecipe(category, contents, capacity))
                .toList());
    }

    public DrawingFluidEmiRecipe(RecipeCategory category, RecipeUtil.Contents contents, int capacity) {
        //this.id = category.getId().withPath(p -> "fluid_withdrawl/" + p + "/" + contents.type().fluid().getId().getPath());
        this.category = category;
        this.background = category.getId().withPath(p -> "textures/gui/" + p + ".png");
        this.contents = contents;
        this.capacity = capacity;
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Identifier getId() {
        return null;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(contents.empty());
    }

    @Override
    public List<TlaStack> getOutputs() {
        return List.copyOf(contents.filled().getStacks());
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of(contents.contents());
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        FluidBoxWidget.create(contents.type(), capacity, 50, 2, 40, 50, widgets);
        widgets.addTexture(TextureConfig.builder().size(120, 64).texture(background).uv(15, 15).build(), 0, 0);
        widgets.addAnimatedTexture(TextureConfig.builder().size(39, 64).texture(background).uv(191, 20).build(), 68, 47, 2000, true, true, true);
        widgets.addSlot(contents.empty(), 2, getCategory().getDisplayHeight() - 16 - 4).markInput();
        widgets.addSlot(contents.contents(), 2 + 16 + 4, getCategory().getDisplayHeight() - 16 - 4).markCatalyst();
        widgets.addSlot(contents.filled(), 107, 45).markOutput();
    }
}
