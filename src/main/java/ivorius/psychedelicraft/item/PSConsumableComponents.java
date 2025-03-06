package ivorius.psychedelicraft.item;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.sound.SoundEvents;

public interface PSConsumableComponents {
    ConsumableComponent FAST_FOOD = ConsumableComponents.food().consumeSeconds(0.8F).build();
    ConsumableComponent DUST = ConsumableComponents.food().consumeSeconds(1.8F).sound(SoundEvents.ENTITY_GENERIC_DRINK).build();
}
