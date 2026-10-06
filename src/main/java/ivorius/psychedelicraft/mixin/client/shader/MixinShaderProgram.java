package ivorius.psychedelicraft.mixin.client.shader;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import ivorius.psychedelicraft.client.render.shader.ProgramUniforms;
import net.minecraft.client.gl.*;

@Mixin(ShaderProgram.class)
abstract class MixinShaderProgram implements AutoCloseable, ProgramUniforms.Holder {
    @Shadow
    private @Final int glRef;

    @Unique
    private final Object2IntMap<String> psychedelicraft_uniformLocations = new Object2IntOpenHashMap<>();
    @Unique
    @Nullable
    private BuiltGemoetryShader psychedelicraft_geometryShader;

    @Inject(method = "set", at = @At("RETURN"))
    private void onSet(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo info) {
        psychedelicraft_geometryShader = GeometryShader.INSTANCE.createShaderBuilder(glRef).build();
    }

    @Override
    @Nullable
    public BuiltGemoetryShader psychedelicraft_getGeometryShader() {
        return psychedelicraft_geometryShader;
    }
}
