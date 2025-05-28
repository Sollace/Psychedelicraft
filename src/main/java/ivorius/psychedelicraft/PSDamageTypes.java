package ivorius.psychedelicraft;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.Difficulty;
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
    RegistryKey<DamageType> TEETH_GRINDING = register("teeth_grinding");
    RegistryKey<DamageType> GLASS_SHARD = register("glass_shard");

    static RegistryKey<DamageType> molotov(Entity target, @Nullable Entity attacker) {
        return target == attacker ? SELF_MOLOTOV : MOLOTOV;
    }

    static DamageSource create(World world, RegistryKey<DamageType> type) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type));
    }

    static DamageSource create(World world, Entity source, @Nullable Entity attacker, RegistryKey<DamageType> type) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type), source, attacker);
    }

    static boolean damage(World world, Entity target, DamageSource source, float amount) {
        if (target instanceof LivingEntity l) {
            Difficulty difficulty = world.getDifficulty();
            float health = l.getHealth();
            amount = switch (difficulty) {
                case PEACEFUL -> Math.min(health - 0.5F, amount);
                case EASY -> amount * 0.5F;
                case HARD -> amount * 0.75F;
                case NORMAL -> amount;
            };
            if (world.getLevelProperties().isHardcore() && amount >= health) {
                if (world.getRandom().nextInt(200) == 0) {
                    amount = Math.min(health - 0.5F, amount);
                } else {
                    amount = (float)(health * world.getRandom().nextTriangular(0.8F, 0.3F));
                }
            }
        }
        return target.damage(source, amount);
    }

    private static RegistryKey<DamageType> register(String name) {
        var key = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Psychedelicraft.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void bootstrap() {}
}
