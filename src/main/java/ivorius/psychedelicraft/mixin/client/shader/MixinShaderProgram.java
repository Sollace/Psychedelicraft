package ivorius.psychedelicraft.mixin.client.shader;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.*;

@Mixin(ShaderProgram.class)
abstract class MixinShaderProgram implements AutoCloseable, BuiltGemoetryShader.Holder {
    @Shadow
    private @Final int glRef;

    @Unique
    @Nullable
    private BuiltGemoetryShader psychedelicraft_geometryShader;

    @Inject(method = "set", at = @At("RETURN"))
    private void onSet(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo info) {
        psychedelicraft_geometryShader = GeometryShader.INSTANCE.getShaderBuilder().build(glRef);
    }

    @Override
    @Nullable
    public BuiltGemoetryShader getUniformData() {
        return psychedelicraft_geometryShader;
    }
}
