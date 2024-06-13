package ivorius.psychedelicraft.compat.emi;

import java.util.List;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DryingEmiRecipe implements EmiRecipe, PSRecipe {

    private final Identifier id;
    private final DryingRecipe recipe;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    public DryingEmiRecipe(Identifier id, DryingRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        EmiIngredient input = EmiIngredient.of(recipe.getInput());
        this.input = Stream.generate(() -> input).limit(9).toList();
        this.output = List.of(EmiStack.of(recipe.getResult(MinecraftClient.getInstance().world.getRegistryManager())));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return Main.DRYING_TABLE.category();
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
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 74;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Identifier background = Psychedelicraft.id("textures/gui/drying_table.png");
        int y = 12;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18 + y);
        int sOff = 0;
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            if (s >= 0 && s < input.size()) {
                widgets.addSlot(input.get(s), i % 3 * 18, (i / 3 * 18) + y);
            } else {
                widgets.addSlot(EmiStack.of(ItemStack.EMPTY), i % 3 * 18, (i / 3 * 18) + y);
            }
        }
        widgets.addSlot(output.get(0), 92, 14 + y).large(true).recipeContext(this);
        widgets.addText(EmiPort.ordered(EmiPort.translatable("emi.cooking.experience", recipe.experience())), 58, 55, -1, true);

        widgets.addTexture(background, 95, 0, 25, 25, 177, 20);
        widgets.addAnimatedTexture(background, 60, 18 + y, 25, 20, 177, 42, 50 * recipe.cookTime(), true, false, false).tooltip((mx, my) -> {
            return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.cooking.time", recipe.cookTime() / 20f))));
        });
    }
}
