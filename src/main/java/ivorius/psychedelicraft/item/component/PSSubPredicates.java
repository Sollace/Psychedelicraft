package ivorius.psychedelicraft.item.component;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import ivorius.psychedelicraft.util.compat.ItemSubPredicate;

public interface PSSubPredicates {
    ItemSubPredicate.Type<ItemFluids.Predicate> FLUIDS = register("fluids", ItemFluids.Predicate.CODEC);
    ItemSubPredicate.Type<FluidCapacity.Predicate> FLUID_CAPACITY = register("fluid_capacity", FluidCapacity.Predicate.CODEC);
    ItemSubPredicate.Type<DrinkType.Predicate> DRINK_TYPE = register("drink_type", DrinkType.Predicate.CODEC);

    private static <V, T extends ItemSubPredicate<V>> ItemSubPredicate.Type<T> register(String id, Codec<T> codec) {
        return ItemSubPredicate.register(Psychedelicraft.id(id), new ItemSubPredicate.Type<>(codec));
    }

    static void bootstrap() {}
}
