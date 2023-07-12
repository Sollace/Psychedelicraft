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
import net.minecraft.client.render.Shader;

@Mixin(Shader.class)
abstract class MixinShaderProgram implements GlShader, AutoCloseable {
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
        RenderSystem.assertOnRenderThread();
        GeometryShader.INSTANCE.getSamplers().keySet().forEach(samplerName -> {
            if (GlUniform.getUniformLocation(getProgramRef(), samplerName) != -1) {
                samplerNames.add(samplerName);
                samplers.put(samplerName, null);
            }
        });
        GeometryShader.INSTANCE.addUniforms(this, uniform -> {
            if (GlUniform.getUniformLocation(getProgramRef(), uniform.getName()) != -1) {
                uniforms.add(uniform);
            }
        });
    }
}

@Mixin(GLImportProcessor.class)
abstract class MixinGLImportProcessor {
    @ModifyVariable(method = "readSource(Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
    private String modifySource(String source) {
        return GeometryShader.INSTANCE.injectShaderSources(source);
    }
}

@Mixin(Program.class)
abstract class MixinShaderStage {
    @Inject(method = "loadProgram", at = @At("HEAD"))
    private static void onLoad(Program.Type type, String name, InputStream stream, String domain, GLImportProcessor loader, CallbackInfoReturnable<Integer> info) throws IOException {
        GeometryShader.INSTANCE.setup(type, name, stream, domain, loader);
    }
}
