/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

import com.mojang.blaze3d.systems.RenderSystem;

public class DrugHallucinationEntity extends DrugHallucination {
    public static final EntityType<?>[] ENTITIES = new EntityType<?>[] {
        EntityType.CREEPER,
        EntityType.ZOMBIE,
        EntityType.BLAZE,
        EntityType.ENDERMAN,
        EntityType.COW,
        EntityType.SHEEP,
        EntityType.PIG,
        EntityType.OCELOT,
        EntityType.WOLF,
        EntityType.SILVERFISH,
        EntityType.VILLAGER,
        EntityType.IRON_GOLEM,
        EntityType.SNOW_GOLEM,
        EntityType.HORSE
    };

    public Entity entity;

    public int entityMaxTicks;
    public float rotationYawPlus;

    public float[] color;

    public float scale;

    public DrugHallucinationEntity(PlayerEntity player) {
        super(player);

        Random rand = player.getRandom();

        entity = ENTITIES[player.world.random.nextInt(ENTITIES.length)].create(player.world);
        entity.setPosition(
                player.getX() + rand.nextDouble() * 50D - 25D,
                player.getY() + rand.nextDouble() * 10D - 5D,
                player.getZ() + rand.nextDouble() * 50D - 25D
        );
        entity.setVelocity(
                (rand.nextDouble() - 0.5D) / 10D,
                (rand.nextDouble() - 0.5D) / 10D,
                (rand.nextDouble() - 0.5D) / 10D
        );
        entity.setYaw(rand.nextInt(360));
        entityMaxTicks = (rand.nextInt(59) + 3) * 20;
        rotationYawPlus = rand.nextFloat() * 10 * (rand.nextBoolean() ? 0 : 1);

        color = new float[]{
                rand.nextFloat(),
                rand.nextFloat(),
                rand.nextFloat()
        };

        scale = 1;
        while (rand.nextFloat() < 0.3F) {
            scale *= rand.nextFloat() * 2.7f + 0.3F;
        }

        if (scale > 20) {
            scale = 20;
        }
    }

    @Override
    public void update()
    {
        super.update();

        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();

        entity.prevYaw = entity.getYaw();
        entity.prevPitch = entity.getPitch();

        entity.setPosition(entity.getPos().add(entity.getVelocity()));
        entity.setYaw(MathHelper.wrapDegrees(entity.getYaw() + rotationYawPlus));

        if (entity instanceof LivingEntity living)
        {
            double velocity = entity.getPos().subtract(entity.prevX, 0, entity.prevZ).horizontalLength() * 4;
            living.limbAngle += (Math.min(velocity, 1) / 3F - living.limbAngle) * 0.4F;
            living.limbDistance += living.limbAngle;
        }
    }

    @Override
    public boolean isDead() {
        return entityTicksAlive >= entityMaxTicks;
    }

    @Override
    public void render(float tickDelta, float dAlpha)
    {
        float alpha = Math.min(1, MathHelper.sin((float) Math.min(entityTicksAlive, entityMaxTicks - 2) / (float) (entityMaxTicks - 2) * MathHelper.PI) * 18);

        if (alpha <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
        float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(color[0], color[1], color[2], alpha * dAlpha);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.scale(scale, scale, scale);
        Vec3d camera = client.gameRenderer.getCamera().getPos();

        client.getEntityRenderDispatcher().render(
                entity,
                x - camera.x, y - camera.y, z - camera.z, yaw, tickDelta, matrices,
                client.getBufferBuilders().getEntityVertexConsumers(),
                0xF000F0
        );
        RenderSystem.setShaderColor(1, 1, 1, 1);

        matrices.pop();
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public void receiveChatMessage(String message, LivingEntity entity) { }
}
