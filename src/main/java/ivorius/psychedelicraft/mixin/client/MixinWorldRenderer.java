package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.RenderPhase;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
abstract class MixinWorldRenderer {
    private static final String SKY = "renderSky(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Fog;)V";
    private static final String CLOUDS = "renderClouds(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/option/CloudRenderMode;Lnet/minecraft/util/math/Vec3d;FIF)V";

    @Inject(method = SKY, at = @At("HEAD"))
    private void beforeRenderSky(CallbackInfo info) {
        RenderPhase.SKY.push();
    }

    @Inject(method = CLOUDS, at = @At("HEAD"))
    private void beforeRenderClouds(CallbackInfo info) {
        RenderPhase.CLOUDS.push();
    }

    @Inject(method = { CLOUDS, SKY }, at = @At("RETURN"))
    private void afterRenderClouds(CallbackInfo info) {
        RenderPhase.pop();
    }
}
