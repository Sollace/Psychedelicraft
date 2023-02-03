package ivorius.psychedelicraft.client.item;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.FluidContainer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.*;
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
        ModelPredicateProviderRegistry.register(Psychedelicraft.id("flying"), (stack, world, entity, seed) -> {
            return stack.hasNbt() && stack.getNbt().getBoolean("flying") ? 1 : 0;
        });
        ModelPredicateProviderRegistry.register(PSItems.WINE_GRAPE_LATTICE, Psychedelicraft.id("age"), (stack, world, entity, seed) -> {
            return stack.getDamage() / 10F;
        });
        ModelPredicateProviderRegistry.register(Psychedelicraft.id("filled"), (stack, world, entity, seed) -> {
            if (stack.getItem() instanceof BongItem item) {
                return item.hasUsableConsumable(entity) ? 1 : 0;
            }
            return FluidContainer.of(stack).getFluid(stack).isEmpty() ? 0 : 1;
        });
        ColorProviderRegistry.ITEM.register((stack, layer) -> layer > 0 ? -1 : PSItems.HARMONIUM.getColor(stack), PSItems.HARMONIUM);
        ColorProviderRegistry.ITEM.register((stack, layer) -> {
            if (layer == 0 && stack.getItem() instanceof DyeableItem dyeable) {
                return ((DyeableItem)stack.getItem()).getColor(stack);
            }
            if (layer == 1) {
                SimpleFluid fluid = FluidContainer.of(stack).getFluid(stack);
                if (!fluid.isEmpty()) {
                    return fluid.getTranslucentColor(stack);
                }
            }
            return -1;
        }, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.GLASS_CHALICE, PSItems.STONE_CUP, PSItems.WOODEN_MUG, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL);
        ColorProviderRegistry.ITEM.register((stack, layer) -> {
            if (layer == 0) {
                SimpleFluid fluid = FluidContainer.of(stack).getFluid(stack);
                if (!fluid.isEmpty()) {
                    return fluid.getTranslucentColor(stack);
                }
            }
            return -1;
        }, PSItems.FILLED_GLASS_BOTTLE);
    }
}
