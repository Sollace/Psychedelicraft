/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RastaHeadModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class RastaHeadHallucination extends AbstractEntityHallucination {
    private static final Identifier TEXTURE = Psychedelicraft.id("textures/drug/cannabis/rasta_head_hallucination.png");

    private final LookControl lookControl;

    private final Model modelRastaHead = new RastaHeadModel();

    public RastaHeadHallucination(PlayerEntity playerEntity) {
        super(playerEntity);
        this.maxAge = (playerEntity.getRandom().nextInt(59) + 120) * 20;
        this.scale = 1;

        this.entity = EntityType.PIG.create(playerEntity.world);
        this.entity.setPosition(playerEntity.getPos());
        this.lookControl = ((MobEntity)entity).getLookControl();

//        this.chatBot = new ChatBotRastahead(playerEntity.getRNG(), playerEntity);
    }

    @Override
    protected void animateEntity() {
        this.lookControl.lookAt(player);
        this.lookControl.tick();

        double radius = 5;
        int seed = player.age + (entity.getId() * 3);
        Vec3d wanted = player.getEyePos().add(
            MathHelper.sin(seed / 50F) * radius,
            MathHelper.sin(seed / 10F) + (entity.getId() % 5) - 1,
            MathHelper.cos(seed / 50F) * radius
        );

        double totalDist = wanted.distanceTo(entity.getPos());

        Vec3d vel = entity.getVelocity().multiply(0.9D);

        vel = wanted.subtract(entity.getPos()).normalize().multiply(Math.log((float)totalDist));

        entity.setVelocity(vel);
        entity.setPosition(entity.getPos().add(vel));
    }

    @Override
    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        yaw = 180 - MathHelper.lerp(tickDelta, ((LivingEntity)entity).prevHeadYaw, ((LivingEntity)entity).headYaw);

        matrices.translate(x, y, z);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));

        var dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        modelRastaHead.render(matrices, vertices.getBuffer(modelRastaHead.getLayer(TEXTURE)), dispatcher.getLight(entity, tickDelta), 0, 1, 1, 1, 1);
    }
}
