package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.RenderPhase;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
abstract class MixinGameRenderer {
    @Shadow
    private @Final Camera camera;

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"))
    private void onRenderWorld(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        DrugRenderer.INSTANCE.distortScreen(matrices, camera, tickDelta);
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void beforeRenderWorld(RenderTickCounter tickCounter, CallbackInfo info) {
        RenderPhase.WORLD.push();
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void afterRenderWorld(RenderTickCounter tickCounter, CallbackInfo info) {
        RenderPhase.pop();
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        DrugRenderer.INSTANCE.getPostEffects().setupDimensions(width, height);
    }
}
