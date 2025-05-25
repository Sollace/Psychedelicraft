package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaCategory;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

class WorldInteractionPSRecipe implements TlaRecipe {
    static final int PLUS_WIDTH = 13;
    static final int ARROW_WIDTH = 24;

    private final Identifier id;
    private final List<TlaIngredient> left;
    private final List<TlaIngredient> right;

    private final List<TlaStack> outputs;

    private int leftSize = 1, rightSize = 1, outputSize = 1;

    private final TlaCategory category;

    public static void generate(TlaCategory category, PluginContext context) {

        var customFluids = SimpleFluid.REGISTRY.streamEntries()
                .filter(entry -> entry.value().getPhysical().getStandingFluid().getBucketItem() == PSItems.FILLED_BUCKET)
                .toList();
        var removedFluidInteractionRecipeIds = customFluids.stream()
                .flatMap(entry -> entry.getKey().stream())
                .map(RegistryKey::getValue)
                .map(WorldInteractionPSRecipe::bucketFillingId)
                .toList();

        context.addGenerator(client -> customFluids.stream().map(fluid -> {
            return (TlaRecipe)new WorldInteractionPSRecipe(category,
                    recipeId("psy_bucket_filling", fluid.getKey().orElseThrow().getValue()),
                    TlaStack.of(Items.BUCKET).asIngredient(),
                    TlaIngredient.ofFluids(List.of(fluid.value().getPhysical().getStandingFluid())),
                    RecipeUtil.toTlaStack(Items.BUCKET.getDefaultStack(), fluid.value().getDefaultStack())
            );
        }).toList());
        context.addGenerator(client -> List.of(
            new WorldInteractionPSRecipe(category, Psychedelicraft.id("morning_glory_flowers"), TlaStack.of(PSItems.MORNING_GLORY_LATTICE).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEARS), TlaStack.of(PSItems.MORNING_GLORY)),
            new WorldInteractionPSRecipe(category, Psychedelicraft.id("juniper_berries"), TlaStack.of(PSItems.FRUITING_JUNIPER_LEAVES).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEARS), TlaStack.of(PSItems.JUNIPER_BERRIES)),
            new WorldInteractionPSRecipe(category, Psychedelicraft.id("wine_grapes"), TlaStack.of(PSItems.WINE_GRAPE_LATTICE).asIngredient(), TlaIngredient.ofItemTag(ConventionalItemTags.SHEARS), TlaStack.of(PSItems.WINE_GRAPES))
        ));

        context.removeRecipes(recipe -> removedFluidInteractionRecipeIds.contains(recipe.getId()));
    }

    public WorldInteractionPSRecipe(TlaCategory category, Identifier id, TlaIngredient left, TlaIngredient right, TlaStack output) {
        this.category = category;
        this.id = id.withPrefixedPath("/");
        this.left = List.of(left);
        this.right = List.of(right);
        this.outputs = List.of(output);
        this.leftSize = this.left.size();
        this.rightSize = this.right.size();
        this.outputSize = this.outputs.size();
    }

    @Override
    public TlaCategory getCategory() {
        return category;
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

        widgets.addTexture(TextureConfig.builder().texture(Main.WIDGETS).size(13, 13).uv(82, 0).build(), (lr + rl) / 2 - PLUS_WIDTH / 2, 0);

        widgets.addArrow((rr + ol) / 2 - ARROW_WIDTH / 2, 0, false);

        int yo = 0;//(slotHeight - leftHeight) * 9;
        for (int i = 0; i < left.size(); i++) {
            TlaIngredient wi = left.get(i);
            widgets.addSlot(wi, i % leftSize * 18, yo + i / leftSize * 18).markInput();
        }

        yo = 0;//(slotHeight - rightHeight) * 9;
        for (int i = 0; i < right.size(); i++) {
            TlaIngredient wi = right.get(i);
            widgets.addSlot(wi, rl + i % rightSize * 18, yo + i / rightSize * 18).markCatalyst();
        }

        yo = 0;//(slotHeight - outputHeight) * 9;
        for (int i = 0; i < outputs.size(); i++) {
            TlaStack wi = outputs.get(i);
            widgets.addSlot(wi, ol + i % outputSize * 18, yo + i / outputSize * 18).markOutput();
        }
    }

    static Identifier bucketFillingId(Identifier id) {
        return recipeId("bucket_filling", id);
    }

    static Identifier recipeId(String type, Identifier id) {
        return Identifier.of("emi", "/emi/" + type + "/" + subId(id));
    }

    static String subId(Identifier id) {
        return id.getNamespace() + "/" + id.getPath();
    }
}
