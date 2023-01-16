package ivorius.psychedelicraft.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.Function;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.registry.entry.*;

class FeatureRegistry {
    private static final List<ConfiguredEntry> CONFIGURED_FEATURES = new ArrayList<>();
    private static final List<PlacedEntry> PLACED_FEATURES = new ArrayList<>();
    static {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).ifPresent(registry -> {
                CONFIGURED_FEATURES.forEach(entry -> {
                    Registry.register(registry, entry.key(), entry.factory().get());
                });
            });
            registries.getOptional(RegistryKeys.PLACED_FEATURE).ifPresent(registry -> {
                var lookup = registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).orElseThrow();
                PLACED_FEATURES.forEach(entry -> {
                    Registry.register(registry, entry.key(), entry.factory().apply(lookup.getEntry(entry.configuration()).orElseThrow()));
                });
            });
        });
    }

    public static void registerConfiguredFeature(
            RegistryKey<ConfiguredFeature<?, ?>> featureKey,
            Supplier<ConfiguredFeature<?, ?>> factory) {
        CONFIGURED_FEATURES.add(new ConfiguredEntry(featureKey, factory));
    }

    public static void registerPlacedFeature(
            RegistryKey<PlacedFeature> key,
            RegistryKey<ConfiguredFeature<?, ?>> configuration,
            Function<RegistryEntry<ConfiguredFeature<?, ?>>, PlacedFeature> factory) {
        PLACED_FEATURES.add(new PlacedEntry(key, configuration, factory));
    }

    record ConfiguredEntry (
        RegistryKey<ConfiguredFeature<?, ?>> key,
        Supplier<ConfiguredFeature<?, ?>> factory
    ) {}

    record PlacedEntry (
            RegistryKey<PlacedFeature> key,
            RegistryKey<ConfiguredFeature<?, ?>> configuration,
            Function<RegistryEntry<ConfiguredFeature<?, ?>>, PlacedFeature> factory
        ) {}
}
