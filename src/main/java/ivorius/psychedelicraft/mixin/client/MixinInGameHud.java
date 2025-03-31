package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {
    @Shadow
    private int ticks;
    private int originalTicks;

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        DrugRenderer.INSTANCE.onRenderOverlay(context, tickCounter);
    }

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
