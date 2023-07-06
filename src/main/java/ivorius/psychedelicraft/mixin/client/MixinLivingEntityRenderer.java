package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.VillagerDataContainer;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    MixinLivingEntityRenderer() { super(null); }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
                shift = Shift.AFTER))
    private void onRender(
            T entity,
            float yaw, float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            CallbackInfo into) {
        if (entity instanceof PlayerEntity player) {
            DrugRenderer.INSTANCE.poseModel(player, (BipedEntityModel<?>)getModel());
        }
    }

    @ModifyVariable(method = "setupTransforms", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float changeBodyYaw(float bodyYaw, T entity) {
        if (entity instanceof VillagerDataContainer v && v.getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            bodyYaw += AddictTaskListProvider.getShakeAmount(entity);
        }

        return bodyYaw;
    }
}
