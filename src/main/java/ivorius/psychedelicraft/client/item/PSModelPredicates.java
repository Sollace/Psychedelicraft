package ivorius.psychedelicraft.client.item;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.items.SmokeableItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSModelPredicates {
    static void bootstrap() {
        ModelPredicateProviderRegistry.register(Psychedelicraft.id("use_time"), (stack, world, entity, seed) -> {
            if (entity == null || entity.getActiveItem() != stack || !(stack.getItem() instanceof SmokeableItem)) {
                return 0;
            }
            float progress = (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / (float)stack.getMaxUseTime();
            return MathHelper.floor(progress * ((SmokeableItem)stack.getItem()).getStages());
        });
    }
}
