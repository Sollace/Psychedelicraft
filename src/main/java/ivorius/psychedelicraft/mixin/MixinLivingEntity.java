package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.entity.LivingEntityDuck;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements LivingEntityDuck {
    private MixinLivingEntity() { super(null, null); }

    @Override
    @Invoker
    public abstract void invokeJump();

    @ModifyVariable(method = "modifyAppliedDamage", at = @At("HEAD"), index = 1, argsOnly = true)
    private float modifyDamageOnModifyAppliedDamage(float amount) {
        return amount * DrugProperties.of(this).map(properties -> {
            return properties.getModifier(Drug.PAIN_SUPPRESSION);
        }).orElse(1F);
    }
}
