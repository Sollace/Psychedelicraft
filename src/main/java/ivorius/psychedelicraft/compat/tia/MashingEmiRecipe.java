package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

class MashingEmiRecipe implements PSRecipe {
    private static final Identifier TEXTURE = Psychedelicraft.id("textures/gui/wooden_vat.png");
    private static final TextureConfig IN_VAT = TextureConfig.builder().size(60, 44).texture(TEXTURE).uv(57, 15).build();
    private static final TextureConfig OUT_VAT = TextureConfig.builder().size(30, 20).texture(TEXTURE).uv(28, 10).textureSize(128, 128).build();

    private final RecipeEntry<MashingRecipe> recipe;
    private final List<TlaIngredient> input;

    private final ItemFluids outputFluid;
    private final TlaStack output;

    private final ItemFluids baseFluids;
    private final TlaIngredient fluidIngredient;

    public MashingEmiRecipe(RecipeEntry<MashingRecipe> recipe) {
        this.recipe = recipe;
        this.input = RecipeUtil.grouped(
                recipe.value().getIngredients().stream().map(TlaIngredient::ofIngredient)
        ).toList();
        this.outputFluid = recipe.value().result().ofAmount(FluidVolumes.VAT);
        this.output = RecipeUtil.toTlaStack(outputFluid);
        this.baseFluids = recipe.value().baseFluid().ofAmount(FluidVolumes.VAT);
        this.fluidIngredient = RecipeUtil.toIngredient(baseFluids);
    }

    @Override
    public RecipeCategory getCategory() {
        return RecipeCategory.VAT;
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
        return List.of(output);
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of(fluidIngredient);
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        int x = 7;
        int y = 0;

        widgets.addArrow(60 + x, 21 + y, false).addTooltip(Text.translatable("gui.psychedelicraft.recipe.stewing_time", recipe.value().stewTime()));
        widgets.addAnimatedArrow(60 + x, 21 + y, 5000);
        var inBox = FluidBoxWidget.create(baseFluids, FluidVolumes.VAT, x, 17 + y, 60, 31, widgets);
        widgets.addTexture(IN_VAT, x, 5 + y);

        widgets.addSlot(fluidIngredient, 2, getCategory().getDisplayHeight() - 16 - 4).markCatalyst();

        x += 85;
        y += 15;

        var outBox = FluidBoxWidget.create(outputFluid, FluidVolumes.VAT, x, 8 + y, 30, 16, widgets);
        outBox.addExclusion(widgets.addSlot(output, 6 + x, 8 + y).disableBackground().markOutput());
        widgets.addTexture(OUT_VAT, x, 5 + y);

        int ingredientsStackCount = input.size();

        int maxIngredientsWidth = 3 * 16;
        int maxIngredientsHeight = 2 * 16;

        int ingredientsLeft = 9 + (maxIngredientsWidth - (Math.min(3, ingredientsStackCount) * 16)) / 2;
        int ingredientsTop = 2 + (maxIngredientsHeight - (ingredientsStackCount / 4) * 8) / 2;

        for (int i = 0; i < ingredientsStackCount; i++) {
            int row = i / 3;
            int col = i % 3;
            inBox.addExclusion(
                widgets.addSlot(input.get(i),
                        col * 16 + ingredientsLeft,
                        row * 16 + ingredientsTop + (col % 2) * 8
                ).disableBackground().markInput()
            );
        }
    }
}
