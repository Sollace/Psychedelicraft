package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"))
    private void onRender(MatrixStack stack, float tickDelta, CallbackInfo info) {
        DrugRenderer.INSTANCE.onRenderOverlay(stack, tickDelta);
    }
}
