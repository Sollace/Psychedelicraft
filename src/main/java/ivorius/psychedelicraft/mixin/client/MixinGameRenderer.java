package ivorius.psychedelicraft.mixin.client;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.client.render.shader.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;

@Mixin(GameRenderer.class)
abstract class MixinGameRenderer {
    @Inject(method = "renderWorld",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/render/Camera.update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
                shift = Shift.AFTER)
    )
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        DrugRenderer.INSTANCE.distortScreen(matrices, tickDelta);
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void beforeRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        RenderPhase.WORLD.push();
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void afterRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        RenderPhase.pop();
    }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/gl/Framebuffer.beginWrite(Z)V",
                shift = Shift.BEFORE)
    )
    private void onBeforeFrameEnd(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        DrugRenderer.INSTANCE.getPostEffects().render(tickDelta);
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        DrugRenderer.INSTANCE.getPostEffects().setupDimensions(width, height);
    }

    @Inject(method = "loadPrograms(Lnet/minecraft/resource/ResourceFactory;)V", at = @At(
                value = "INVOKE",
                target = "java/util/List.add(Ljava/lang/Object;)Z",
                ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void onLoadPrograms(ResourceFactory factory, CallbackInfo info,
            List<ShaderStage> stages,
            List<Pair<ShaderProgram, Consumer<ShaderProgram>>> programs) throws IOException {
        CoreShaderRegistrationCallback.EVENT.invoker().call(factory, programs);
    }
}
