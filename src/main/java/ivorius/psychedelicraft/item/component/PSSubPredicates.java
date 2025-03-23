package ivorius.psychedelicraft.item.component;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSSubPredicates {
    ComponentPredicate.Type<ItemFluids.Predicate> FLUIDS = register("fluids", ItemFluids.Predicate.CODEC);
    ComponentPredicate.Type<FluidCapacity.Predicate> FLUID_CAPACITY = register("fluid_capacity", FluidCapacity.Predicate.CODEC);
    ComponentPredicate.Type<DrinkType.Predicate> DRINK_TYPE = register("drink_type", DrinkType.Predicate.CODEC);

    private static <T extends ComponentPredicate> ComponentPredicate.Type<T> register(String id, Codec<T> codec) {
        return Registry.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, Psychedelicraft.id(id), new ComponentPredicate.Type<>(codec));
    }

    static void bootstrap() {
    }
}
