package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.AbstractBlock;

@Mixin(AbstractBlock.Settings.class)
public interface MixinAbstractBlockSettings {
    @Accessor
    void setOffsetter(AbstractBlock.Offsetter offsetter);
}
