package ivorius.psychedelicraft.mixin.client.shader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;

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

    @ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private List<String> modifySamplers(List<String> samplers) {
        ShaderProgram self = (ShaderProgram)(Object)this;
        List<String> names = new ArrayList<>(samplers);
        Set<String> distincts = new HashSet<>(samplers);
        GeometryShader.INSTANCE.getSamplers().forEach((samplerName, sampler) -> {
            if (distincts.add(samplerName) && GlUniform.getUniformLocation(self.getGlRef(), samplerName) != -1) {
                names.add(samplerName);
            }
        });
        return names;
    }

    @Inject(method = "set", at = @At("HEAD"))
    private void onLoadReferences(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo info) {
        ShaderProgram self = (ShaderProgram)(Object)this;
        GeometryShader.INSTANCE.addUniforms(uniform -> {
            int location = GlUniform.getUniformLocation(self.getGlRef(), uniform.getName());
            if (location != -1) {
                this.uniforms.add(uniform);
                uniformsByName.put(uniform.getName(), uniform);
                uniform.setLocation(location);
            }
        });
    }

    @Inject(method = "initializeUniforms", at = @At("RETURN"))
    private void onInitializeUniforms(VertexFormat.DrawMode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, float screenWidth, float screenHeight, CallbackInfo info) {
        ShaderProgram self = (ShaderProgram)(Object)this;
        GeometryShader.INSTANCE.getSamplers().forEach((samplerName, sampler) -> {
            int location = GlUniform.getUniformLocation(self.getGlRef(), samplerName);
            if (location != -1) {
                samplerTextures.put(samplerName, sampler.get());
            }
        });
    }
}
