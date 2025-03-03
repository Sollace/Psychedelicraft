package ivorius.psychedelicraft.util;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry.Reference;

public interface BootstrapUtil {
    static <T> void bootstrapDynamically(RegistryKey<Registry<T>> registryRef, RegistryBuilder.BootstrapFunction<T> bootstrapFunction) {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(registryRef).ifPresent(registry -> {
                bootstrapFunction.run(BootstrapUtil.createRegisterable(registries, registry));
            });
        });
    }

    static <T> Registerable<T> createRegisterable(DynamicRegistryView registries, Registry<T> registry) {
        return new Registerable<>() {
            private final Supplier<DynamicRegistryManager> manager = Suppliers.memoize(() -> registries.asDynamicRegistryManager());

            @Override
            public Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
                return Registry.registerReference(registry, key, value);
            }

            @Override
            public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
                return manager.get().getOrThrow(registryRef);
            }
        };
    }

}
