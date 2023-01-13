package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity implements DrugPropertiesContainer {
    MixinPlayerEntity() {super(null, null);}

    @Nullable
    private DrugProperties drugProperties;

    @Override
    public DrugProperties getDrugProperties() {
        if (drugProperties == null) {
            drugProperties = new DrugProperties((PlayerEntity)(Object)this);
        }
        return drugProperties;
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        getDrugProperties().onTick();
    }

    @Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void onWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo info) {
        if (!getDrugProperties().onAwoken()) {
            info.cancel();
        }
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (!world.isClient) {
            getDrugProperties().trySleep(pos).ifPresent(reason -> {
                ((PlayerEntity)(Object)this).sendMessage(reason, true);

                info.setReturnValue(Either.right(Unit.INSTANCE));
            });
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(info.getReturnValue() * getDrugProperties().getModifier(Drug.DIG_SPEED));
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo info) {
        tag.put("psychedelicraft_drug_properties", getDrugProperties().toNbt());
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo info) {
        if (tag.contains("psychedelicraft_drug_properties", NbtElement.COMPOUND_TYPE)) {
            getDrugProperties().fromNbt(tag.getCompound("psychedelicraft_drug_properties"));
        }
    }
}
