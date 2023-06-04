/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import ivorius.psychedelicraft.client.render.PassThroughVertexConsumer;
import ivorius.psychedelicraft.client.render.RenderLayerUtil;

public abstract class AbstractEntityHallucination extends Hallucination {

    protected final Random random;
    protected final Entity entity;

    protected int maxAge;

    protected float[] color = {1, 1, 1, 1};

    protected float scale;

    private float dAlpha;
    private final PassThroughVertexConsumer.Parameters colourSpace = new PassThroughVertexConsumer.Parameters().color((parent, r, g, b, a) -> {
        parent.color(color[0], color[1], color[2], dAlpha);
    });

    public AbstractEntityHallucination(PlayerEntity player, Entity entity) {
        super(player);
        this.random = player.getRandom();
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return age >= maxAge;
    }

    @Override
    public void update() {
        super.update();

        entity.age++;
        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();

        entity.prevYaw = entity.getYaw();
        entity.prevPitch = entity.getPitch();

        if (entity instanceof LivingEntity living) {
            living.prevHeadYaw = living.headYaw;
        }

        animateEntity();

        if (entity instanceof LivingEntity living) {
            living.updateLimbs(false);
        }
    }

    protected abstract void animateEntity();

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, float dAlpha) {
        this.dAlpha = Math.min(1,
                MathHelper.sin(
                        (float) Math.min(age, maxAge - 2) / (float) (maxAge - 2)
                        * MathHelper.PI) * 18) * dAlpha;

        if (MathHelper.approximatelyEquals(this.dAlpha, 0)) {
            return;
        }

        Vec3d cameraPos = camera.getPos();

        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX()) - cameraPos.x;
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) - cameraPos.y;
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()) - cameraPos.z;
        float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch(tickDelta));
        float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw(tickDelta));

        entity.lastRenderX = entity.getX();
        entity.lastRenderY = entity.getY();
        entity.lastRenderZ = entity.getZ();

        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-x, -y, -z);

        renderModel(matrices, layer -> {
            return PassThroughVertexConsumer.of(vertices.getBuffer(getRenderLayer(layer)), colourSpace);
        }, x, y, z, pitch, yaw, tickDelta);
        matrices.pop();
    }

    protected RenderLayer getRenderLayer(RenderLayer layer) {
        var renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer != null) {
            return RenderLayerUtil.getTexture(layer).map(RenderLayer::getEntityTranslucent).orElseGet(RenderLayer::getTranslucent);
        }
        return layer;
    }

    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        var dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadows(false);
        dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, vertices, dispatcher.getLight(entity, tickDelta));
        dispatcher.setRenderShadows(true);
    }
}
