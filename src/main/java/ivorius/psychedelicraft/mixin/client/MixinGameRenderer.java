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
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;

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

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        DrugRenderer.INSTANCE.getPostEffects().setupDimensions(width, height);
    }

    @Inject(method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V", at = @At(
                value = "INVOKE",
                target = "java/util/List.add(Ljava/lang/Object;)Z",
                ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void onLoadPrograms(ResourceManager factory, CallbackInfo info,
            List<Program> stages,
            List<Pair<Shader, Consumer<Shader>>> programs) throws IOException {
        CoreShaderRegistrationCallback.EVENT.invoker().call(factory, programs);
    }
}
