package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

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
    private boolean shaking;

    @Override
    public float getActualSaturationLevel() {
        return saturationLevel;
    }

    @Override
    public int getActualFoodLevel() {
        return foodLevel;
    }

    @Override
    public void setShanksShaking(boolean shaking) {
        this.shaking = shaking;
    }

    @Inject(method = "add(IF)V", at = @At("HEAD"))
    private void onAdd(int food, float saturationModifier, CallbackInfo info) {
        if (lockedState != null && !lockedState.full() && foodLevel + food > 20) {
            overeating += food / 8F;
        }
    }

    @ModifyReturnValue(method = "isNotFull()Z", at = @At("RETURN"))
    private boolean onIsNotFull(boolean notFull) {
        return lockedState != null ? !lockedState.full() : notFull;
    }

    @ModifyReturnValue(method = "getFoodLevel()I", at = @At("RETURN"))
    private int onGetFoodLevel(int foodLevel) {
        return lockedState != null ? (int)lockedState.hunger().toFloat(foodLevel) : foodLevel;
    }

    @ModifyReturnValue(method = "getSaturationLevel()F", at = @At("RETURN"))
    private float onGetSaturationLevel(float saturation) {
        return shaking ? -1 : lockedState != null ? lockedState.saturation().toFloat(saturation) : saturation;
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
