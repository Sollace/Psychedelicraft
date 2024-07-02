package ivorius.psychedelicraft.item;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;

public class SyringeItem extends DrinkableItem {
    public SyringeItem(Settings settings) {
        super(settings, FluidVolumes.SYRINGE, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.INJECT);
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        if (!(target instanceof LivingEntity l)) {
            return 0;
        }
        float hurtTime = l.maxHurtTime == 0 ? 0 : l.hurtTime / (float)l.maxHurtTime;
        return (float)target.getRandom().nextTriangular(3, 1.5) * (1 + hurtTime);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Hand hand = stack == attacker.getMainHandStack() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        var effect = new FluidParticleEffect(PSParticles.FLUID_SPLASH, PSFluids.TOMATO);
        //for (int i = 0; i < 10; i++) {
            ((ServerWorld)target.getWorld()).spawnParticles(effect, target.getParticleX(1), target.getEyeY(), target.getParticleZ(1), 10, 0, 0, 0, 0.3F);
        //}
        if (!(target instanceof PlayerEntity) || !canUse(stack, target.getWorld(), target)) {
            return false;
        }
        attacker.setStackInHand(hand, use(stack, target.getWorld(), target, attacker));

        return true;
    }

    @Override
    @Nullable
    protected SoundEvent getUseSound() {
        return PSSounds.ITEM_SYRINGE_INJECT;
    }
}
