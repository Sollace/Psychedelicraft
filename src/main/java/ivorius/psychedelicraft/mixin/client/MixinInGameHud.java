package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {
    @Shadow
    private @Final MinecraftClient client;
    @Shadow
    private int ticks;
    private Integer originalTicks;

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At("HEAD"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo info) {
        DrugRenderer.INSTANCE.onRenderOverlay(context);
    }

    @Inject(method = "renderStatusBars", at = @At(value = "CONSTANT", args = "stringValue=food"))
    private void onBeforeRenderShank(DrawContext context, CallbackInfo info) {
        originalTicks = ticks;
        ticks = DrugProperties.of(client.player).getStomach().setVisibleState(ticks);
    }

    @Inject(method = "renderStatusBars", at = @At(value = "CONSTANT", args = "stringValue=air"))
    private void onAfterRenderShank(DrawContext context, CallbackInfo info) {
        if (originalTicks != null) {
            ticks = originalTicks;
            originalTicks = null;
        }
        ((LockableHungerManager)client.player.getHungerManager()).setShanksShaking(false);
    }
}
