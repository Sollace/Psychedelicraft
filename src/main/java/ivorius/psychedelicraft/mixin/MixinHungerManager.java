package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.entity.drug.GluttonyManager;
import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
abstract class MixinHungerManager implements LockableHungerManager, GluttonyManager {
    @Shadow
    private int foodLevel;
    @Shadow
    private float saturationLevel;

    @Nullable
    private State lockedState;

    private float overeating;

    @Inject(method = "add(IF)Z", at = @At("HEAD"))
    private void onAdd(int food, float saturationModifier, CallbackInfo info) {
        if (lockedState != null && !lockedState.full() && foodLevel + food > 20) {
            overeating += food / 8F;
        }
    }

    @Inject(method = "isNotFull()Z", at = @At("HEAD"), cancellable = true)
    private void onIsNotFull(CallbackInfoReturnable<Boolean> info) {
        if (lockedState != null) {
            info.setReturnValue(!lockedState.full());
        }
    }

    @Inject(method = "getFoodLevel()I", at = @At("HEAD"), cancellable = true)
    private void onGetFoodLevel(CallbackInfoReturnable<Integer> info) {
        if (lockedState != null) {
            info.setReturnValue((int)lockedState.hunger().toFloat(foodLevel));
        }
    }

    @Inject(method = "getSaturationLevel()F", at = @At("HEAD"), cancellable = true)
    private void onGetSaturationLevel(CallbackInfoReturnable<Float> info) {
        if (lockedState != null) {
            info.setReturnValue(lockedState.saturation().toFloat(saturationLevel));
        }
    }

    @Inject(method = { "setFoodLevel(I)V", "setSaturationLevel(F)V" }, at = @At("HEAD"), cancellable = true)
    private void onSetFoodOrSaturationLevel(CallbackInfo info) {
        if (lockedState != null) {
            info.cancel(); // XXX: Can't really handle sets whilst the food is locked
        }
    }

    @Override
    public float getOvereating() {
        return this.overeating;
    }

    @Override
    public void setOvereating(float overeating) {
        this.overeating = overeating;
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
