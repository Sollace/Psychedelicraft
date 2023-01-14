package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(Mouse.class)
abstract class MixinGameRenderer {
    @Inject(method = "renderWorld",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/Camera.update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
                shift = Shift.AFTER),
            cancellable = true
    )
    public void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DrugProperties.of((Entity)mc.player).ifPresent(properties -> {
            DrugRenderer.INSTANCE.distortScreen(matrices, tickDelta, mc.player, mc.player.age, properties);
        });
    }
}
