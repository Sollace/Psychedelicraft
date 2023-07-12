package ivorius.psychedelicraft.world.gen;

import java.util.function.Supplier;

import ivorius.psychedelicraft.Psychedelicraft;

import java.util.function.Function;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import net.minecraft.world.gen.feature.*;

class FeatureRegistry {

    public static RegistryEntry<ConfiguredFeature<?, ?>> registerConfiguredFeature(
            RegistryKey<ConfiguredFeature<?, ?>> featureKey,
            Supplier<ConfiguredFeature<?, ?>> factory) {
        return addCasted(BuiltinRegistries.CONFIGURED_FEATURE, featureKey.getValue(), factory.get());
    }

    public static RegistryEntry<PlacedFeature> registerPlacedFeature(
            RegistryKey<PlacedFeature> key,
            RegistryEntry<ConfiguredFeature<?, ?>> configuration,
            Function<RegistryEntry<ConfiguredFeature<?, ?>>, PlacedFeature> factory) {
        return addCasted(BuiltinRegistries.PLACED_FEATURE, key.getValue(), factory.apply(configuration));
    }

    @SuppressWarnings("unchecked")
    public static <V extends T, T> RegistryEntry<V> addCasted(Registry<T> registry, Identifier id, V value) {
        RegistryEntry<T> registryEntry = BuiltinRegistries.add(registry, id, value);
        return (RegistryEntry<V>)registryEntry;
    }

    public static RegistryKey<PlacedFeature> createPlacement(String id) {
        return RegistryKey.of(Registry.PLACED_FEATURE_KEY, Psychedelicraft.id(id));
    }

}
