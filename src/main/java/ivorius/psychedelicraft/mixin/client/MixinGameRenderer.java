package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.RenderPhase;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
abstract class MixinGameRenderer {
    @Shadow
    private @Final Camera camera;

    @Shadow
    private @Final Pool pool;

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

    @Inject(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/render/WorldRenderer.drawEntityOutlinesFramebuffer()V"))
    private void onAfterWorldRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        DrugRenderer.INSTANCE.onAfterRenderWorld(pool);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/hud/InGameHud.render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        DrugRenderer.INSTANCE.onRenderOverlay(context, tickCounter);
    }
}
