/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import com.mojang.blaze3d.systems.RenderSystem;

public abstract class AbstractEntityHallucination extends DrugHallucination {

    public Entity entity;

    public int entityMaxTicks;

    public float[] color;

    public float scale;

    public AbstractEntityHallucination(PlayerEntity player) {
        super(player);
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return entityTicksAlive >= entityMaxTicks;
    }

    @Override
    public void update() {
        super.update();

        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();

        entity.prevYaw = entity.getYaw();
        entity.prevPitch = entity.getPitch();

        animateEntity();

        if (entity instanceof LivingEntity living) {
            double velocity = entity.getPos().subtract(entity.prevX, 0, entity.prevZ).horizontalLength() * 4;
            living.limbAngle += (Math.min(velocity, 1) / 3F - living.limbAngle) * 0.4F;
            living.limbDistance += living.limbAngle;
        }
    }

    protected abstract void animateEntity();

    @Override
    public void render(float tickDelta, float dAlpha) {
        float alpha = Math.min(1, MathHelper.sin((float) Math.min(entityTicksAlive, entityMaxTicks - 2) / (float) (entityMaxTicks - 2) * MathHelper.PI) * 18);

        if (alpha <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
        float pitch = entity.getPitch(tickDelta);
        float yaw = entity.getYaw(tickDelta);

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(color[0], color[1], color[2], alpha * dAlpha);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.scale(scale, scale, scale);
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        renderModel(matrices, client.getBufferBuilders().getEntityVertexConsumers(), x - camera.x, y - camera.y, z - camera.z, pitch, yaw, tickDelta);
        matrices.pop();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(
                entity,
                x, y, z, yaw, tickDelta, matrices,
                vertices,
                0xF000F0
        );
    }
}
