package ivorius.psychedelicraft.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
abstract class MixinSoundSystem {
    @Inject(method = "getSoundVolume", at = @At("RETURN"), cancellable = true)
    private void getSoundVolume(@Nullable SoundCategory category, CallbackInfoReturnable<Float> info) {
        DrugProperties.of((Entity)MinecraftClient.getInstance().player).ifPresent(properties -> {
            info.setReturnValue(MathHelper.clamp(info.getReturnValueF() * properties.getModifier(Drug.SOUND_VOLUME), 0, 1));
        });
    }
}
