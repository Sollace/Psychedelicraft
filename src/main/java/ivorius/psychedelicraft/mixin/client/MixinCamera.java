package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.render.DrugEffectInterpreter;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;

@Mixin(Camera.class)
abstract class MixinCamera {
    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void onIsThirdPerson(CallbackInfoReturnable<Boolean> info) {
        DrugProperties properties = DrugProperties.of(MinecraftClient.getInstance().player);
        if (properties != null && DrugEffectInterpreter.getOOB(properties)) {
            info.setReturnValue(true);
        }
    }
}
