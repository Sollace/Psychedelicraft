package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import java.util.function.Consumer;

/**
 * Created by lukas on 25.11.14.
 */
public class AgaveFluid extends AlcoholicFluid {

    public AgaveFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public void getDefaultStacks(ItemStack container, Consumer<ItemStack> consumer) {
        boolean isShot = container.isOf(PSItems.SHOT_GLASS);
        settings.states.get().forEach(state -> {
            if (isShot == "tequila".contentEquals(state.entry().value().drinkName())) {
                consumer.accept(ItemFluids.set(container.copy(), state.apply(getDefaultStack())));
            }
        });
    }
}
