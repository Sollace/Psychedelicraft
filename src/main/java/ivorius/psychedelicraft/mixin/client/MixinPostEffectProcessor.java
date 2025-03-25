package ivorius.psychedelicraft.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import ivorius.psychedelicraft.client.render.shader.PostEffectPassSupplier;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;

@Mixin(PostEffectProcessor.class)
abstract class MixinPostEffectProcessor implements PostEffectPassSupplier {
    @Accessor
    @Override
    public abstract List<PostEffectPass> getPasses();

    @Accessor
    @Override
    public abstract void setPasses(List<PostEffectPass> passes);
}
