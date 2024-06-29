package ivorius.psychedelicraft.item.component;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSSubPredicates {
    ItemSubPredicate.Type<ItemFluids.Predicate> FLUIDS = register("fluids", ItemFluids.Predicate.CODEC);

    private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String id, Codec<T> codec) {
        return Registry.register(Registries.ITEM_SUB_PREDICATE_TYPE, Psychedelicraft.id(id), new ItemSubPredicate.Type<>(codec));
    }

    static void bootstrap() {
    }
}
