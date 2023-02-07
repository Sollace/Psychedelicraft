package ivorius.psychedelicraft;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSDamageSources {
    DamageSource ALCOHOL_POISONING = new DamageSource("alcohol_poisoning").setBypassesArmor().setUnblockable();
    DamageSource RESPIRATORY_FAILURE = new DamageSource("respiratory_failure").setBypassesArmor().setUnblockable();
    DamageSource STROKE = new DamageSource("stroke").setBypassesArmor().setUnblockable();
    DamageSource HEART_FAILURE = new DamageSource("heart_failure").setBypassesArmor().setUnblockable();
    DamageSource KIDNEY_FAILURE = new DamageSource("kidney_failure").setBypassesArmor().setUnblockable();
    DamageSource IN_SLEEP = new DamageSource("in_sleep").setBypassesArmor().setUnblockable();

    static DamageSource molotov(Entity projectile, Entity target, @Nullable Entity attacker) {
        return new ProjectileDamageSource(target == attacker ? "molotov.self" : "molotov", projectile, attacker).setProjectile();
    }
}
