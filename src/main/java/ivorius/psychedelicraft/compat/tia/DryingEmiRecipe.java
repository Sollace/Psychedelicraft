package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import java.util.stream.Stream;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.client.screen.DryingTableScreen;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

class DryingEmiRecipe implements PSRecipe {
    private static final TextureConfig SUN = TextureConfig.builder().size(25, 25).texture(DryingTableScreen.TEXTURE).uv(177, 20).build();
    private static final TextureConfig ARROW_FILL = TextureConfig.builder().size(25, 20).texture(DryingTableScreen.TEXTURE).uv(177, 42).build();

    private final RecipeEntry<DryingRecipe> recipe;
    private final List<TlaIngredient> input;
    private final List<TlaStack> output;

    public DryingEmiRecipe(RecipeEntry<DryingRecipe> recipe) {
        this.recipe = recipe;
        TlaIngredient input = TlaIngredient.ofIngredient(recipe.value().getInput());
        this.input = Stream.generate(() -> input).limit(9).toList();
        this.output = List.of(TlaStack.of(recipe.value().getResult(MinecraftClient.getInstance().world.getRegistryManager())));
    }

    @Override
    public RecipeCategory getCategory() {
        return RecipeCategory.DRYING_TABLE;
    }

    @Override
    public Identifier getId() {
        return recipe.id();
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return input;
    }

    @Override
    public List<TlaStack> getOutputs() {
        return output;
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of();
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        int y = 12;
        widgets.addArrow(60, 18 + y, false);
        int sOff = 0;
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            widgets.addSlot(s >= 0 && s < input.size() ? input.get(s) : TlaIngredient.EMPTY, i % 3 * 18, (i / 3 * 18) + y).markInput();
        }
        widgets.addSlot(output.get(0), 92, 14 + y).makeLarge().markOutput();
        widgets.addText(Text.translatable("emi.cooking.experience", recipe.value().experience()), 58, 55, -1, true);

        widgets.addTexture(SUN, 95, 0);
        widgets.addAnimatedTexture(ARROW_FILL, 60, 18 + y, 50 * recipe.value().cookTime(), true, false, false).addTooltip(
            Text.translatable("emi.cooking.time", recipe.value().cookTime() / 20f)
        );
    }

}
