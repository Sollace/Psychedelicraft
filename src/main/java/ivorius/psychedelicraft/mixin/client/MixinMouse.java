package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.SmoothCameraHelper;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
abstract class MixinMouse {
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void beforeUpdateMouse(CallbackInfo info) {
        SmoothCameraHelper.INSTANCE.setCursorDelta((float)cursorDeltaX, (float)cursorDeltaY);
    }

    @Inject(method = "updateMouse", at = @At("RETURN"))
    private void onUpdateMouse(CallbackInfo info) {
        SmoothCameraHelper.INSTANCE.applyCameraChange();
    }
}
