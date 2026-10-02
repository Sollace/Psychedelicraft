package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.AddictTaskListProvider;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M>  {
    MixinLivingEntityRenderer() {super(null);}

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo info) {
        if (state instanceof VillagerEntityRenderState v && v.getVillagerData() != null && v.getVillagerData().profession().matchesKey(PSTradeOffers.DRUG_ADDICT_PROFESSION)) {
            float shakeAmount = AddictTaskListProvider.getShakeAmount(entity);
            state.bodyYaw += shakeAmount;
            v.hurt |= Math.abs(shakeAmount) > 5F;
        }
    }
}

@Mixin(PlayerEntityModel.class)
abstract class MixinPlayerEntityModel {
    // models are posed when the render queue is drawn, so this is the last point we can adjust them
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("RETURN"))
    private void onSetAngles(PlayerEntityRenderState state, CallbackInfo info) {
        DrugRenderer.INSTANCE.poseModel(state, (BipedEntityModel<?>)(Object)this);
    }
}
