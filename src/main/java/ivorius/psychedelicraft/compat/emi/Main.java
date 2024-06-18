package ivorius.psychedelicraft.compat.emi;

import java.util.Objects;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;

public class Main implements EmiPlugin {

    static final Comparison COMPARE_FLUID = Comparison.of(
            (a, b) -> a.getKey() == b.getKey() && ItemFluids.of(a.getItemStack()).isRoughlyEqual(ItemFluids.of(b.getItemStack())),
            s -> Objects.hash(s.getKey(), ItemFluids.of(s.getItemStack()).getHash())
    );

    @Override
    public void register(EmiRegistry registry) {
        setComparison(registry, COMPARE_FLUID,
                PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL
        );
    }

    static void setComparison(EmiRegistry registry, Comparison comparison, Object...keys) {
        for (var key : keys) {
            registry.setDefaultComparison(key, c -> comparison);
        }
    }
}
