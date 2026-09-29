package ivorius.psychedelicraft.mixin.client.shader;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import ivorius.psychedelicraft.client.render.shader.PostEffectPassSupplier;
import ivorius.psychedelicraft.client.render.shader.ProgramUniforms;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;

@Mixin(PostEffectPass.class)
abstract class MixinPostEffectPass implements PostEffectPassSupplier.Pass {
    @Shadow
    private @Final RenderPipeline pipeline;

    @Unique
    private boolean disabled;

    @Unique
    @Nullable
    private Supplier<Map<String, float[]>> uniformUpdater;

    @Accessor
    @Override
    public abstract String getId();

    @Override
    public void setDisabled() {
        this.disabled = true;
    }

    @Override
    public void setUniformUpdater(Supplier<Map<String, float[]>> updater) {
        this.disabled = false;
        this.uniformUpdater = updater;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void beforeRender(FrameGraphBuilder builder, Map<Identifier, Handle<Framebuffer>> handles, GpuBufferSlice slice, CallbackInfo info) {
        if (disabled) {
            disabled = false;
            info.cancel();
            return;
        }
        if (uniformUpdater != null) {
            ProgramUniforms.setPostUniforms(pipeline, uniformUpdater.get());
        }
    }
}
