package ivorius.psychedelicraft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;

import org.spongepowered.asm.mixin.injection.At.Shift;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(HeldItemRenderer.class)
abstract class MixinHeldItemRenderer {
    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At(
            value = "FIELD",
            target = "net/minecraft/client/render/item/HeldItemRenderer$HandRenderType.renderMainHand:Z",
            opcode = Opcodes.GETFIELD,
            shift = Shift.BEFORE
        ))
    private void onRenderItem(float tickDelta, MatrixStack matrices, OrderedRenderCommandQueue queue, ClientPlayerEntity player, int light, CallbackInfo info) {
        DrugRenderer.INSTANCE.distortHand(matrices);
    }
}
