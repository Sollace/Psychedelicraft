package ivorius.psychedelicraft.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;

public class Main implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        setComparison(registry, Comparison.compareComponents(),
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        );
        SimpleFluid.REGISTRY.forEach(fluid -> {
            registry.setDefaultComparison(fluid, Comparison.compareComponents());
        });
    }

    static void setComparison(EmiRegistry registry, Comparison comparison, Object...keys) {
        for (var key : keys) {
            registry.setDefaultComparison(key, c -> comparison);
        }
    }
}
