package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import ivorius.psychedelicraft.block.MashTubBlock;
import ivorius.psychedelicraft.entity.TouchingWaterAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.*;

@Mixin(Entity.class)
abstract class MixinEntity implements TouchingWaterAccessor {
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Override
    @Accessor
    public abstract void setTouchingWater(boolean touchingWater);

    @Inject(method = "updateMovementInFluid", at = @At("RETURN"), cancellable = true)
    private void onUpdateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValueZ()) {
            Entity self = (Entity)(Object)this;
            BlockPos.stream(self.getBoundingBox().contract(0.001)).map(pos -> {
                BlockState state = self.world.getBlockState(pos);
                if (state.getBlock() instanceof MashTubBlock tub) {
                    return tub.getFluidHeight(self.world, state, pos, tag);
                }
                return -1;
            }).filter(l -> l > 0).findFirst().ifPresent(level -> {
                fluidHeight.put(tag, level);
                info.setReturnValue(true);
            });
        }
    }
}
