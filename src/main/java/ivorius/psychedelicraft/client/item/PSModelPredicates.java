package ivorius.psychedelicraft.client.item;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluids.SimpleFluid;
import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.items.PSItems;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.DyeableItem;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSModelPredicates {
    static void bootstrap() {
        ModelPredicateProviderRegistry.register(Psychedelicraft.id("using"), (stack, world, entity, seed) -> {
            if (entity == null || entity.getActiveItem() != stack) {
                return 0;
            }
            return entity.getItemUseTimeLeft() > 0 ? 1 : 0;
        });
        ModelPredicateProviderRegistry.register(PSItems.bong, Psychedelicraft.id("filled"), (stack, world, entity, seed) -> {
            return PSItems.bong.hasUsableConsumable(entity) ? 1 : 0;
        });
        ColorProviderRegistry.ITEM.register((stack, layer) -> layer > 0 ? -1 : PSItems.harmonium.getColor(stack), PSItems.harmonium);
        ColorProviderRegistry.ITEM.register((stack, layer) -> {
            if (layer == 0 && stack.getItem() instanceof DyeableItem dyeable) {
                return ((DyeableItem)stack.getItem()).getColor(stack);
            }
            if (stack.getItem() instanceof FluidContainerItem container) {
                SimpleFluid fluid = container.getFluid(stack);
                if (!fluid.isEmpty()) {
                    return container.getFluid(stack).getColor(stack);
                }
            }
            return -1;
        }, PSItems.bottle, PSItems.molotovCocktail, PSItems.glassChalice, PSItems.stoneCup, PSItems.woodenMug);
    }
}
