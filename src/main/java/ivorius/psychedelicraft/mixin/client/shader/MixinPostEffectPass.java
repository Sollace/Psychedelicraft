package ivorius.psychedelicraft.mixin.client.shader;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import ivorius.psychedelicraft.client.render.shader.PostEffectPassSupplier;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;

@Mixin(PostEffectPass.class)
abstract class MixinPostEffectPass implements PostEffectPassSupplier.Pass {
    @Unique
    private boolean disabled;

    @Accessor
    @Override
    public abstract String getId();

    @Accessor
    @Override
    public abstract RenderPipeline getPipeline();

    @Accessor("uniformBuffers")
    @Override
    public abstract Map<String, GpuBuffer> getUniforms();

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void beforeRender(FrameGraphBuilder builder, Map<Identifier, Handle<Framebuffer>> handles, GpuBufferSlice slice, CallbackInfo info) {
        if (disabled) {
            disabled = false;
            info.cancel();
        }
    }
}
