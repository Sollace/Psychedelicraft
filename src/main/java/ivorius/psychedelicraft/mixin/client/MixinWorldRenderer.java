package ivorius.psychedelicraft.mixin.client;

import java.util.SortedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import ivorius.psychedelicraft.client.render.BlockBreakingProgressAccessor;
import ivorius.psychedelicraft.client.render.RenderPhase;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.BlockBreakingInfo;

@Mixin(WorldRenderer.class)
abstract class MixinWorldRenderer implements BlockBreakingProgressAccessor {
    private static final String SKY = "renderSky(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Fog;)V";
    private static final String CLOUDS = "renderClouds(Lnet/minecraft/client/render/FrameGraphBuilder;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/option/CloudRenderMode;Lnet/minecraft/util/math/Vec3d;FIF)V";

    @Override
    @Accessor
    public abstract Long2ObjectMap<SortedSet<BlockBreakingInfo>> getBlockBreakingProgressions();

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
