package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import ivorius.psychedelicraft.client.render.shader.PostEffectPassSupplier;
import net.minecraft.client.gl.PostEffectPass;

@Mixin(PostEffectPass.class)
abstract class MixinPostEffectPass implements PostEffectPassSupplier.Pass {
    @Accessor
    @Override
    public abstract String getId();
}
