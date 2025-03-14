package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.HardeningRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

class TrayEmiRecipe implements PSRecipe {
    private final RecipeEntry<HardeningRecipe> recipe;

    private final TlaIngredient base;
    private final List<TlaIngredient> impurities;
    private final List<TlaStack> output;

    public TrayEmiRecipe(RecipeEntry<HardeningRecipe> recipe) {
        this.recipe = recipe;
        this.base = RecipeUtil.toIngredient(recipe.value().coreFluid(), 1);
        this.impurities = recipe.value().impurities().stream().map(i -> {
            recipe.value().coreFluid();
            return RecipeUtil.toIngredient(i, 1);
        }).toList();
        this.output = List.of(TlaStack.of(recipe.value().getResult(MinecraftClient.getInstance().world.getRegistryManager())));
    }

    @Override
    public RecipeCategory getCategory() {
        return RecipeCategory.TRAY;
    }

    @Override
    public Identifier getId() {
        return recipe.id();
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(base);
    }

    @Override
    public List<TlaStack> getOutputs() {
        return output;
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return impurities;
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        int y = 4;
        widgets.addSlot(TlaStack.of(PSItems.BOTTLE), 10, y + 13).disableBackground();
        widgets.addSlot(TlaStack.of(PSItems.BUNSEN_BURNER), 12, y + 23).disableBackground().markCatalyst();
        widgets.addArrow(30, y + 23, false);
        widgets.addAnimatedFlame(30, y + 23, 900);

        int ingredientsX = 70;

        if (impurities.isEmpty()) {
            widgets.addSlot(base, ingredientsX, y + 22).markInput();
        } else {
            widgets.addSlot(base, ingredientsX, y + 4).markInput();
            widgets.addTexture(TextureConfig.builder().texture(Main.WIDGETS).size(13, 13).uv(82, 0).build(), ingredientsX + 3, y + 24);

            int gridWidth = impurities.size() * 20;
            int gridLeft = ingredientsX + 10 - gridWidth/2;

            for (int i = 0; i < impurities.size(); i++) {
                widgets.addSlot(impurities.get(i), i % 3 * 18 + gridLeft, (i / 3 * 18) + y + 40).markInput();
            }
        }
        widgets.addArrow(100, y + 23, false);
        widgets.addSlot(output.get(0), 170, y + 20).makeLarge().markOutput();
        widgets.addAnimatedArrow(130, y + 23, Math.max(1, recipe.value().hardeningTime()) * 900);
        widgets.addSlot(TlaStack.of(PSItems.TRAY), 130, y + 23).disableBackground().markCatalyst();
    }

}
