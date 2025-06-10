package ivorius.psychedelicraft.datagen;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.world.gen.PSPlacedFeatures;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.PlacedFeature;

final class CustomStructurePools {
    static void bootstrap(Registerable<StructurePool> registerable) {
        RegistryEntryLookup<PlacedFeature> features = registerable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntryLookup<StructurePool> pools = registerable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry<StructurePool> empty = pools.getOrThrow(StructurePools.EMPTY);
        registerExtra(registerable, "village/plains/houses", new StructurePool(empty, List.of(
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/plains/houses/plains_hot_house_1"), 50),
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/plains/houses/plains_hot_house_2"), 50),
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/plains/houses/plains_vineyard"), 25),
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/plains/houses/lattice_wall_1"), 15)
        ), StructurePool.Projection.RIGID));

        registerExtra(registerable, "village/desert/decor", new StructurePool(empty, List.of(
                Pair.of(StructurePoolElement.ofFeature(features.getOrThrow(PSPlacedFeatures.AGAVE_PATCH_UNCHECKED)), 10),
                Pair.of(StructurePoolElement.ofFeature(features.getOrThrow(PSPlacedFeatures.PEYOTE_PATCH_UNCHECKED)), 1)
        ), StructurePool.Projection.TERRAIN_MATCHING));

        registerExtra(registerable, "village/desert/houses", new StructurePool(empty, List.of(
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/desert/houses/desert_hot_house_1"), 25),
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/desert/houses/desert_hot_house_2"), 50)
        ), StructurePool.Projection.RIGID));

        registerExtra(registerable, "village/taiga/houses", new StructurePool(empty, List.of(
                Pair.of(StructurePoolElement.ofSingle("psychedelicraft:village/taiga/houses/taiga_drying_house_4"), 25)
        ), StructurePool.Projection.RIGID));
    }

    static void register(Registerable<StructurePool> registerable, String name, StructurePool pool) {
        registerable.register(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Psychedelicraft.id(name)), pool);
    }

    static void registerExtra(Registerable<StructurePool> registerable, String name, StructurePool pool) {
        registerable.register(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of(Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE, name)), pool);
    }
}
