package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.item.PSItems;
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
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        boolean isShot = container.asItem() == PSItems.SHOT_GLASS;
        settings.states.get().forEach(state -> {
            boolean isTequila = "tequila".contentEquals(state.entry().value().drinkName());
            if (isShot == isTequila) {
                consumer.accept(state.apply(getDefaultStack(container)));
            }
        });
    }
}
