package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;

@Mixin(FluidState.class)
abstract class MixinFluidState {
    @Inject(method = "getParticle()Lnet/minecraft/particle/ParticleEffect;", at = @At("HEAD"), cancellable = true)
    private void getStateAwareParticle(CallbackInfoReturnable<@Nullable ParticleEffect> info) {
        FluidState self = (FluidState)(Object)this;
        if (self.getFluid() instanceof PlacedFluid p) {
            info.setReturnValue(p.getParticle(self));
        }
    }
}
