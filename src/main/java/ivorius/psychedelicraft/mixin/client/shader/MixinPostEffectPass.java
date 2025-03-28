package ivorius.psychedelicraft.mixin.client.shader;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.shader.PostEffectPassSupplier;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;

@Mixin(PostEffectPass.class)
abstract class MixinPostEffectPass implements PostEffectPassSupplier.Pass {
    @Shadow
    private ShaderProgram program;

    @Unique
    private boolean disabled;

    @Accessor
    @Override
    public abstract String getId();

    @Shadow @Mutable
    private @Final List<PostEffectPipeline.Uniform> uniforms;

    @Nullable
    private PostEffectPassSupplier.UniformUpdater uniformUpdater;

    @Override
    public void setDisabled() {
        this.disabled = true;
    }

    @Override
    public void setUniformUpdater(PostEffectPassSupplier.UniformUpdater updater) {
        this.disabled = false;
        this.uniformUpdater = updater;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void beforeRender(FrameGraphBuilder builder, Map<Identifier, Handle<Framebuffer>> handles, Matrix4f projectionMatrix, CallbackInfo info) {
        if (disabled) {
            disabled = false;
            info.cancel();
        }
        if (uniformUpdater != null) {
            uniforms = uniformUpdater.accept(program);
        }
    }
}
