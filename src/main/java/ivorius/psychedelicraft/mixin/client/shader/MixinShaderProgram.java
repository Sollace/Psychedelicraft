package ivorius.psychedelicraft.mixin.client.shader;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTexture;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.*;

@Mixin(ShaderProgram.class)
abstract class MixinShaderProgram implements AutoCloseable {
    @Shadow
    private @Final List<GlUniform> uniforms;
    @Shadow
    private @Final Map<String, GlUniform> uniformsByName;

    @Shadow
    private @Final List<String> samplers;
    @Shadow
    private @Final Object2ObjectMap<String, GpuTexture> samplerTextures;
    @Shadow
    private @Final IntList samplerLocations;

    @Inject(method = "set", at = @At("RETURN"))
    private void onLoadReferences(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo info) {
        ShaderProgram self = (ShaderProgram)(Object)this;
        GeometryShader.INSTANCE.getSamplers().forEach((samplerName, sampler) -> {
            int location = GlUniform.getUniformLocation(self.getGlRef(), samplerName);
            if (location != -1) {
                this.samplers.add(samplerName);
                samplerLocations.add(location);
                samplerTextures.put(samplerName, sampler.get());
            }
        });
        GeometryShader.INSTANCE.addUniforms(uniform -> {
            if (GlUniform.getUniformLocation(self.getGlRef(), uniform.getName()) != -1) {
                this.uniforms.add(uniform);
                uniformsByName.put(uniform.getName(), uniform);
            }
        });
    }
}
