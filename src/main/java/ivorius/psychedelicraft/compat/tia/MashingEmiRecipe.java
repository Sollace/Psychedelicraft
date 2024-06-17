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
        this.input = RecipeUtil.grouped(recipe.value().getIngredients().stream().map(TlaIngredient::ofIngredient)).toList();
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
        int y = 27;

        widgets.addArrow(60 + x, 21 + y, false);
        widgets.addAnimatedArrow(60 + x, 21 + y, 5000);
        widgets.addCustomWidget(FluidBoxWidget.create(baseFluids, FluidVolumes.VAT, 13 + x, 17 + y, 30, 30));
        widgets.addTexture(IN_VAT, x, 5 + y);

        x += 85;
        y += 15;

        widgets.addCustomWidget(FluidBoxWidget.create(outputFluid, FluidVolumes.VAT, 6 + x, 8 + y, 16, 16));
        widgets.addSlot(output, 6 + x, 8 + y).disableBackground().markOutput();
        widgets.addTexture(OUT_VAT, x, 5 + y);

        int sOff = 0;
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            if (s >= 0 && s < input.size()) {
                widgets.addSlot(input.get(s), (i % 4 * 16) + 7, (i / 4 * 16) + 7).disableBackground().markInput();
            }
        }
    }
}
