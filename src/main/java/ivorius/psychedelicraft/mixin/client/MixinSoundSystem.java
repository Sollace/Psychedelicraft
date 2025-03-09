package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
abstract class MixinSoundSystem {
    @ModifyReturnValue(method = "getSoundVolume", at = @At("RETURN"))
    private float getSoundVolume(float volume) {
        return DrugProperties.of((Entity)MinecraftClient.getInstance().player)
                .map(properties -> MathHelper.clamp(volume * properties.getModifier(Drug.SOUND_VOLUME), 0, 1))
                .orElse(volume);
    }
}
