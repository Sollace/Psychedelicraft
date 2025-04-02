package ivorius.psychedelicraft.mixin.client.iris;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.CompiledShader.Type;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(targets = {"net.irisshaders.iris.shaderpack.programs.ProgramSource"}, remap = false)
abstract class MixinProgramSource {
    @Shadow @Mutable
    private @Final String vertexSource;
    @Shadow @Mutable
    private @Final String fragmentSource;

    @Inject(
        method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lnet/irisshaders/iris/shaderpack/properties/ProgramDirectives;Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;)V",
        at = @At("TAIL")
    )
    private void onInit(String name, String vertexSource, String geometrySource, String tessControlSource, String tessEvalSource, String fragmentSource,
            @Coerce Object directives, @Coerce Object parent, CallbackInfo info) {
        psychedelicraft_modifySources(name);
    }

    @Inject(
        method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;Lnet/irisshaders/iris/shaderpack/properties/ShaderProperties;Lnet/irisshaders/iris/gl/blending/BlendModeOverride;)V",
        at = @At("TAIL")
    )
    private void onInit(String name, String vertexSource, String geometrySource, String tessControlSource, String tessEvalSource, String fragmentSource,
            @Coerce Object parent, @Coerce Object properties, @Coerce Object defaultBlendModeOverride, CallbackInfo info) {
        psychedelicraft_modifySources(name);
    }

    @Unique
    private void psychedelicraft_modifySources(String name) {
        if (MinecraftClient.getInstance().getResourceManager() == null) {
            return;
        }
        System.out.println("Created iris shader shource " + name);
        Identifier id = Identifier.of(name).withPrefixedPath("iris/");
        GeometryShader.INSTANCE.setup(Type.FRAGMENT, id);
        this.fragmentSource = GeometryShader.INSTANCE.injectShaderSources(fragmentSource);

        GeometryShader.INSTANCE.setup(Type.VERTEX, id);
        this.vertexSource = GeometryShader.INSTANCE.injectShaderSources(vertexSource);
    }
}
