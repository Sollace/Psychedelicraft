package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
abstract class MixinHungerManager implements LockableHungerManager {
    @Shadow
    private int foodLevel;
    @Shadow
    private float saturationLevel;

    @Nullable
    private State lockedState;

    @Inject(method = "isNotFull()Z", at = @At("HEAD"), cancellable = true)
    private void onIsNotFull(CallbackInfoReturnable<Boolean> info) {
        if (lockedState != null) {
            info.setReturnValue(!lockedState.full());
        }
    }

    @Inject(method = "getFoodLevel()I", at = @At("HEAD"), cancellable = true)
    private void onGetFoodLevel(CallbackInfoReturnable<Integer> info) {
        if (lockedState != null) {
            info.setReturnValue(Math.max(lockedState.hunger(), foodLevel));
        }
    }

    @Inject(method = "getSaturationLevel()F", at = @At("HEAD"), cancellable = true)
    private void onGetSaturationLevel(CallbackInfoReturnable<Float> info) {
        if (lockedState != null) {
            info.setReturnValue(Math.max(lockedState.saturation(), saturationLevel));
        }
    }

    @Override
    @Nullable
    public State getLockedState() {
        return lockedState;
    }

    @Override
    public void setLockedState(State state) {
        lockedState = state;
    }

    @Override
    public HungerManager getHungerManager() {
        return (HungerManager)(Object)this;
    }
}
