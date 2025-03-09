package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.AddictTaskListProvider;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M>  {
    MixinLivingEntityRenderer() {super(null);}

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/render/entity/model/EntityModel.setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V",
                shift = Shift.AFTER))
    private void onRender(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo info) {
        if (state instanceof PlayerEntityRenderState player) {
            DrugRenderer.INSTANCE.poseModel(player, (BipedEntityModel<?>)getModel());
        }
    }

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo info) {
        if (state instanceof VillagerEntityRenderState v && v.getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            float shakeAmount = AddictTaskListProvider.getShakeAmount(entity);
            state.bodyYaw += shakeAmount;
            v.hurt |= Math.abs(shakeAmount) > 5F;
        }
    }
}
