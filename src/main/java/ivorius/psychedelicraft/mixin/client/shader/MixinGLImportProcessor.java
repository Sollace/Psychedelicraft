package ivorius.psychedelicraft.mixin.client.shader;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.GlImportProcessor;

@Mixin(GlImportProcessor.class)
abstract class MixinGLImportProcessor {
    @ModifyReturnValue(method = "readSource(Ljava/util/List;)Ljava/util/List;", at = @At("RETURN"))
    private List<String> modifySource(List<String> source) {
        return GeometryShader.INSTANCE.injectShaderSources(source);
    }
}