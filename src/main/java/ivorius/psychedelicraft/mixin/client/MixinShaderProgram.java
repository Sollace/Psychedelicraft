package ivorius.psychedelicraft.mixin.client;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.CompiledShader.Type;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

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

@Mixin(ShaderLoader.class)
abstract class MixinShaderLoader {
    @Inject(method = "loadShaderSource(Lnet/minecraft/util/Identifier;Lnet/minecraft/resource/Resource;Lnet/minecraft/client/gl/CompiledShader$Type;Ljava/util/Map;Lcom/google/common/collect/ImmutableMap$Builder;)V", at = @At("HEAD"))
    private static void onLoadShaderSource(Identifier id, Resource resource, Type type, Map<Identifier, Resource> allResources, @SuppressWarnings("rawtypes") Builder builder, CallbackInfo info) {
        GeometryShader.INSTANCE.setup(type, type.createFinder().toResourceId(id));
    }
}
