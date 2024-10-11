package ivorius.psychedelicraft.compat.tia;

import java.util.stream.Stream;

import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.plugin.TlaApiPlugin;
import io.github.mattidragon.tlaapi.api.recipe.TlaStackComparison;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.util.Identifier;

public class Main implements TlaApiPlugin {
    static final Identifier WIDGETS = Psychedelicraft.id("textures/gui/widgets.png");

    @Override
    public void register(PluginContext context) {
        RecipeCategory.bootstrap(context);

        context.getItemComparisons().register(TlaStackComparison.compareComponents(),
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        );
        context.getFluidComparisons().register(TlaStackComparison.compareComponents(), SimpleFluid.REGISTRY.stream().flatMap(f -> {
            return Stream.of(f.getPhysical().getStandingFluid(), f.getPhysical().getFlowingFluid());
        }).toList());
    }
}
