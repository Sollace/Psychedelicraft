package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.util.Identifier;

class WorldInteractionEmiRecipe implements PSRecipe {
    static final int PLUS_WIDTH = 13;
    static final int ARROW_WIDTH = 24;

    private final Identifier id;
    private final List<TlaIngredient> left;
    private final List<TlaIngredient> right;

    private final List<TlaStack> outputs;

    private final int slotHeight = 18;
    private int leftSize = 1, rightSize = 1, outputSize = 1;
    private int leftHeight, rightHeight, outputHeight;

    public static void generate(RecipeCategory category, PluginContext context) {
        context.addGenerator(client -> List.of(
            new WorldInteractionEmiRecipe(Psychedelicraft.id("morning_glory_flowers"), TlaStack.of(PSItems.MORNING_GLORY_LATTICE).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEAR_TOOLS), TlaStack.of(PSItems.MORNING_GLORY)),
            new WorldInteractionEmiRecipe(Psychedelicraft.id("juniper_berries"), TlaStack.of(PSItems.FRUITING_JUNIPER_LEAVES).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEAR_TOOLS), TlaStack.of(PSItems.JUNIPER_BERRIES)),
            new WorldInteractionEmiRecipe(Psychedelicraft.id("wine_grapes"), TlaStack.of(PSItems.WINE_GRAPE_LATTICE).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEAR_TOOLS), TlaStack.of(PSItems.WINE_GRAPES))
        ));
    }

    public WorldInteractionEmiRecipe(Identifier id, TlaIngredient left, TlaIngredient right, TlaStack output) {
        this.id = id;
        this.left = List.of(left);
        this.right = List.of(right);
        this.outputs = List.of(output);
        this.leftSize = this.left.size();
        this.rightSize = this.right.size();
        this.outputSize = this.outputs.size();
    }

    @Override
    public RecipeCategory getCategory() {
        return RecipeCategory.WORLD_INTERACTION;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return left;
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return right;
    }

    @Override
    public List<TlaStack> getOutputs() {
        return outputs;
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        int lr = leftSize * 18;
        int ol = getCategory().getDisplayWidth() - outputSize * 18;
        int rl = (lr + ol) / 2 - rightSize * 9 - 4;
        int rr = rl + rightSize * 18;

        //widgets.addTexture(EmiTexture.PLUS, (lr + rl) / 2 - PLUS_WIDTH / 2, -6 + slotHeight * 9);
        widgets.addArrow((rr + ol) / 2 - ARROW_WIDTH / 2, -8 + slotHeight * 9, false);

        int yo = (slotHeight - leftHeight) * 9;
        for (int i = 0; i < left.size(); i++) {
            TlaIngredient wi = left.get(i);
            widgets.addSlot(wi, i % leftSize * 18, yo + i / leftSize * 18).markInput();
        }

        yo = (slotHeight - rightHeight) * 9;
        for (int i = 0; i < right.size(); i++) {
            TlaIngredient wi = right.get(i);
            widgets.addSlot(wi, rl + i % rightSize * 18, yo + i / rightSize * 18).markCatalyst();
        }

        yo = (slotHeight - outputHeight) * 9;
        for (int i = 0; i < outputs.size(); i++) {
            TlaStack wi = outputs.get(i);
            widgets.addSlot(wi, ol + i % outputSize * 18, yo + i / outputSize * 18).markOutput();
        }
    }
}
