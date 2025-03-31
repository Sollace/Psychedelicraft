package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {
    @Shadow
    private int ticks;
    private int originalTicks;

    @Inject(method = "renderFood", at = @At("HEAD"))
    private void onBeforeRenderShank(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo info) {
        originalTicks = ticks;
        ticks = DrugProperties.of(player).getStomach().setVisibleState(ticks);
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void onAfterRenderShank(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo info) {
        ticks = originalTicks;
        ((LockableHungerManager)player.getHungerManager()).setShanksShaking(false);
    }
}
