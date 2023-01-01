package ivorius.psychedelicraft;

import net.minecraft.entity.damage.DamageSource;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSDamageSources {
    DamageSource ALCOHOL_POISONING = new DamageSource("alcohol_poisoning").setBypassesArmor().setUnblockable();
    DamageSource RESPIRATORY_FAILURE = new DamageSource("respiratory_failure").setBypassesArmor().setUnblockable();
    DamageSource STROKE = new DamageSource("stroke").setBypassesArmor().setUnblockable();
    DamageSource HEART_FAILURE = new DamageSource("heart_failure").setBypassesArmor().setUnblockable();
}
