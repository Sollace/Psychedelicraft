package ivorius.psychedelicraft.mixin.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.CompiledShader.Type;

@Mixin(ShaderProgram.class)
abstract class MixinShaderProgram implements AutoCloseable {
    @Shadow
    private @Final List<GlUniform> uniforms;
    @Shadow
    private @Final Map<String, Object> samplers;
    @Shadow
    private @Final List<String> samplerNames;

    @Inject(method = "bind()V", at = @At("HEAD"))
    private void onBind(CallbackInfo info) {
        GeometryShader.INSTANCE.getSamplers().forEach((name, sampler) -> samplers.put(name, sampler.get()));
    }

    @Inject(method = "loadReferences()V", at = @At("HEAD"))
    private void onLoadReferences(CallbackInfo info) {
        ShaderProgram self = (ShaderProgram)(Object)this;
        RenderSystem.assertOnRenderThread();
        GeometryShader.INSTANCE.getSamplers().keySet().forEach(samplerName -> {
            if (GlUniform.getUniformLocation(self.getGlRef(), samplerName) != -1) {
                samplerNames.add(samplerName);
                samplers.put(samplerName, null);
            }
        });
        GeometryShader.INSTANCE.addUniforms(uniform -> {
            if (GlUniform.getUniformLocation(self.getGlRef(), uniform.getName()) != -1) {
                uniforms.add(uniform);
            }
        });
    }
}

@Mixin(GlImportProcessor.class)
abstract class MixinGLImportProcessor {
    @ModifyVariable(method = "readSource(Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
    private String modifySource(String source) {
        return GeometryShader.INSTANCE.injectShaderSources(source);
    }
}

@Mixin(CompiledShader.class)
abstract class MixinShaderStage {
    @Inject(method = "load", at = @At("HEAD"))
    private static void onLoad(Type type, String name, InputStream stream, String domain, GlImportProcessor loader, CallbackInfoReturnable<Integer> info) throws IOException {
        GeometryShader.INSTANCE.setup(type, domain, name);
    }
}
