/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.RastaHeadModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.joml.Quaternionf;

public class DrugHallucinationRastaHead extends AbstractEntityHallucination {
    private static final Identifier TEXTURE = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "rastaHeadTexture.png");

    public final LookControl lookControl;

    public final Model modelRastaHead = new RastaHeadModel();

    public DrugHallucinationRastaHead(PlayerEntity playerEntity) {
        super(playerEntity);
        this.entityMaxTicks = (playerEntity.getRandom().nextInt(59) + 120) * 20;

        this.entity = EntityType.PIG.create(playerEntity.world);
        this.entity.setPosition(playerEntity.getPos());
        this.lookControl = ((MobEntity)entity).getLookControl();

//        this.chatBot = new ChatBotRastahead(playerEntity.getRNG(), playerEntity);
    }

    @Override
    protected void animateEntity() {
        this.lookControl.lookAt(player);
        this.lookControl.tick();

        Vec3d wanted = player.getPos().add(
            MathHelper.sin(player.age / 50.0f) * 5.0f,
            0,
            MathHelper.cos(player.age / 50.0f) * 5.0f
        );

        double totalDist = wanted.distanceTo(entity.getPos());

        if (totalDist > 3) {
            entity.setVelocity(wanted.subtract(entity.getPos()).multiply(0.05D / totalDist));
        } else {
            entity.setVelocity(entity.getVelocity().multiply(0.9D));
        }

        this.entity.setPosition(this.entity.getPos().add(entity.getVelocity()));
    }

    @Override
    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        yaw = -MathHelper.lerp(tickDelta, ((LivingEntity)entity).prevHeadYaw, ((LivingEntity)entity).headYaw);
        matrices.translate(x, y, z);
        matrices.multiply(new Quaternionf().rotateXYZ(180, yaw, pitch));
        modelRastaHead.render(matrices, vertices.getBuffer(modelRastaHead.getLayer(TEXTURE)), 0, 0, 1, 1, 1, 1);
    }
}
