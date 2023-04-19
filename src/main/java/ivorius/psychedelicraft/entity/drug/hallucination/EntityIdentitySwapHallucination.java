package ivorius.psychedelicraft.entity.drug.hallucination;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.util.Pool;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityIdentitySwapHallucination extends Hallucination {

    private final EntityType<?> targetType;
    private final Pool<EntityType<?>> transformedType;

    @Nullable
    private Selection selection;

    public EntityIdentitySwapHallucination(PlayerEntity player, EntityType<?> targetType, Pool<EntityType<?>> transformedType) {
        super(player);
        this.targetType = targetType;
        this.transformedType = transformedType;
    }

    @Nullable
    public Entity matchOrAttach(Entity entity) {

        if (entity.getType() != targetType || (selection != null && !entity.getUuid().equals(selection.selection().getUuid()))) {
            return null;
        }

        if (selection == null) {
            selection = new Selection(entity, transformedType.get(entity.getWorld().getRandom()));
        }

        return selection.attachment();
    }

    @Override
    public void update() {
        super.update();
        if (selection != null) {
            selection.update();
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, float alpha) {
    }

    @Override
    public boolean isDead() {
        if (age >= 300 && selection != null) {
            selection.selection().setSilent(false);
        }
        return age > 300;
    }

    @Override
    public int getMaxHallucinations() {
        return 100;
    }

    record Selection(Entity selection, Entity attachment) {
        Selection(Entity selection, EntityType<?> attachmentType) {
            this(selection, attachmentType.create(selection.getWorld()));
            attachment.setSilent(true);
            attachment.copyFrom(selection);
        }

        public void update() {
            attachment.age++;
            attachment.updatePositionAndAngles(
                    selection.getPos().x, selection.getPos().y, selection.getPos().z,
                    selection.getYaw(), selection.getPitch()
            );
            attachment.setBodyYaw(selection.getBodyYaw());

            attachment.lastRenderX = selection.lastRenderX;
            attachment.lastRenderY = selection.lastRenderY;
            attachment.lastRenderZ = selection.lastRenderZ;
            attachment.prevX = selection.prevX;
            attachment.prevY = selection.prevY;
            attachment.prevZ = selection.prevZ;
            attachment.prevYaw = selection.prevYaw;
            attachment.prevPitch = selection.prevPitch;
            attachment.setOnGround(selection.isOnGround());

            if (attachment instanceof LivingEntity living) {
                living.prevHeadYaw = ((LivingEntity)selection).prevHeadYaw;
                living.headYaw = ((LivingEntity)selection).headYaw;
            }

            attachment.tick();
        }
    }
}
