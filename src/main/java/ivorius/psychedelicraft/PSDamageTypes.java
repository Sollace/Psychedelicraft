package ivorius.psychedelicraft;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public interface PSDamageTypes {
    List<RegistryKey<DamageType>> REGISTRY = new ArrayList<>();

    RegistryKey<DamageType> ALCOHOL_POSIONING = register("alcohol_poisoning");
    RegistryKey<DamageType> RESPIRATORY_FAILURE = register("respiratory_failure");
    RegistryKey<DamageType> STROKE = register("stroke");
    RegistryKey<DamageType> HEART_FAILURE = register("heart_failure");
    RegistryKey<DamageType> HEART_ATTACK = register("heart_attack");
    RegistryKey<DamageType> KIDNEY_FAILURE = register("kidney_failure");
    RegistryKey<DamageType> IN_SLEEP = register("in_sleep");
    RegistryKey<DamageType> OVER_EATING = register("over_eating");
    RegistryKey<DamageType> MOLOTOV = register("molotov");
    RegistryKey<DamageType> SELF_MOLOTOV = register("self_molotov");
    RegistryKey<DamageType> OVERDOSE = register("overdose");

    static RegistryKey<DamageType> molotov(Entity target, @Nullable Entity attacker) {
        return target == attacker ? SELF_MOLOTOV : MOLOTOV;
    }

    static DamageSource create(World world, RegistryKey<DamageType> type) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type));
    }

    static DamageSource create(World world, Entity source, @Nullable Entity attacker, RegistryKey<DamageType> type) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), source, attacker);
    }

    private static RegistryKey<DamageType> register(String name) {
        var key = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Psychedelicraft.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.DAMAGE_TYPE).ifPresent(registry -> {
                REGISTRY.forEach(key -> {
                    Registry.register(registry, key.getValue(), new DamageType(key.getValue().getNamespace() + "." + key.getValue().getPath(), 0));
                });
            });
        });
    }
}
