package ivorius.psychedelicraft.mixin;

import java.util.stream.DoubleStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.entity.TouchingWaterAccessor;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;

@Mixin(Entity.class)
abstract class MixinEntity implements TouchingWaterAccessor {
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Override
    @Accessor
    public abstract void setTouchingWater(boolean touchingWater);

    private FluidState collidedFluid = Fluids.EMPTY.getDefaultState();

    @Redirect(method = "updateMovementInFluid", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"
    ))
    private Vec3d captureFluid(FluidState state, BlockView view, BlockPos pos) {
        if (state.isIn(FluidTags.WATER)) {
            collidedFluid = state;
        }
        return state.getVelocity(view, pos);
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"))
    private void clearCollidedFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        if (tag == FluidTags.WATER) {
            collidedFluid = Fluids.EMPTY.getDefaultState();
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("RETURN"), cancellable = true)
    private void onUpdateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValueZ()) {
            Entity self = (Entity)(Object)this;
            Box box = self.getBoundingBox().expand(0.001);
            BlockPos.stream(self.getBoundingBox().contract(0.001)).flatMapToDouble(pos -> {
                BlockState state = self.getWorld().getBlockState(pos);
                if (!(state.getBlock() instanceof FluidFilled tub)) {
                    return DoubleStream.empty();
                }

                return tub.getContainedFluid(self.getWorld(), state, pos).stream()
                        .filter(fluidState -> fluidState.isIn(tag))
                        .mapToDouble(fluidState -> {
                    if (fluidState.isIn(tag)) {
                        if (tag == FluidTags.WATER) {
                            collidedFluid = fluidState;
                        }
                        Box fluidBox = tub.getFluidCollisionBox(self.getWorld(), state, pos);
                        if (fluidBox.intersects(box)) {
                            return fluidBox.getYLength();
                        }
                    }
                    return -1;
                });
            }).filter(l -> l > 0).findFirst().ifPresent(level -> {
                fluidHeight.put(tag, level);
                info.setReturnValue(true);
            });
        }
    }

    @ModifyArg(method = "onSwimmingStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private ParticleEffect replaceSplashParticle(ParticleEffect parameters) {
        SimpleFluid fluid = SimpleFluid.of(collidedFluid.getFluid());
        if (fluid.isCustomFluid()) {
            if (parameters == ParticleTypes.BUBBLE) {
                return new FluidParticleEffect(PSParticles.FLUID_BUBBLE, fluid.getStack(collidedFluid, 1));
            }
            if (parameters == ParticleTypes.SPLASH) {
                return new FluidParticleEffect(PSParticles.FLUID_SPLASH, fluid.getStack(collidedFluid, 1));
            }
        }
        return parameters;
    }
}
