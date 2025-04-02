package ivorius.psychedelicraft.mixin.client.iris;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.CompiledShader.Type;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(targets = {"net.irisshaders.iris.shaderpack.programs.ProgramSource"}, remap = false)
abstract class MixinProgramSource {
    @Shadow
    private @Final String name;

    @Unique
    private @Nullable Optional<String> recomputedVertexSource;
    @Unique
    private @Nullable Optional<String> recomputedFragmentSource;

    @Inject(method = "getVertexSource()Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void onGetVertexSource(CallbackInfoReturnable<Optional<String>> info) {
        if (recomputedVertexSource == null) {
            if (MinecraftClient.getInstance().getResourceManager() == null) {
                if (info.getReturnValue().isPresent()) {
                    Psychedelicraft.LOGGER.info("Iris is initialising too early!");
                }
                return;
            }
            GeometryShader.INSTANCE.setup(Type.VERTEX, Identifier.of(name).withPrefixedPath("iris/"));
            recomputedVertexSource = Optional.ofNullable(GeometryShader.INSTANCE.injectShaderSources(info.getReturnValue().orElse(null)));
        }
        info.setReturnValue(recomputedVertexSource);
    }

    @Inject(method = "getFragmentSource()Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void onGetFragmentSource(CallbackInfoReturnable<Optional<String>> info) {
        if (recomputedFragmentSource == null) {
            if (MinecraftClient.getInstance().getResourceManager() == null) {
                if (info.getReturnValue().isPresent()) {
                    Psychedelicraft.LOGGER.info("Iris is initialising too early!");
                }
                return;
            }
            GeometryShader.INSTANCE.setup(Type.FRAGMENT, Identifier.of(name).withPrefixedPath("iris/"));
            recomputedFragmentSource = Optional.ofNullable(GeometryShader.INSTANCE.injectShaderSources(info.getReturnValue().orElse(null)));
        }
        info.setReturnValue(recomputedFragmentSource);
    }
}
