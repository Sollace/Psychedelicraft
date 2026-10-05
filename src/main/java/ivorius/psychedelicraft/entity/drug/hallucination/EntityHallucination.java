/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import ivorius.psychedelicraft.entity.TouchingWaterAccessor;

public class EntityHallucination<E extends LivingEntity> extends Hallucination {

    private final E entity;

    public int maxAge;

    public float[] color = {1, 1, 1};

    public float scale;

    private float rotationYawPlus;

    private int fadeOutTicks = -1;

    private final Identifier type;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T> T createEntity(World world, TagKey<EntityType<?>> entityTypes) {
        return (T)world.getRegistryManager().getOrThrow(RegistryKeys.ENTITY_TYPE)
                .getOptional(entityTypes)
                .orElseThrow()
                .getRandom(world.random)
                .map(RegistryEntry::value)
                .orElse((EntityType)EntityType.PIG)
                .create(world, SpawnReason.EVENT);
    }

    public EntityHallucination(Identifier type, PlayerEntity player, E entity) {
        super(player);
        this.type = type;
        this.entity = entity;

        entity.setPosition(
                player.getX() + random.nextDouble() * 50D - 25D,
                player.getY() + random.nextDouble() * 10D - 5D,
                player.getZ() + random.nextDouble() * 50D - 25D
        );
        entity.setVelocity(
                (random.nextDouble() - 0.5D) / 10D,
                (random.nextDouble() - 0.5D) / 10D,
                (random.nextDouble() - 0.5D) / 10D
        );
        entity.setYaw(random.nextInt(360));
        maxAge = (random.nextInt(59) + 3) * 20;
        rotationYawPlus = random.nextFloat() * 10 * (random.nextBoolean() ? 0 : 1);

        color = new float[] {
            random.nextFloat(),
            random.nextFloat(),
            random.nextFloat()
        };

        scale = 1;
        while (random.nextFloat() < 0.3F) {
            scale *= random.nextFloat() * 2.7f + 0.3F;
        }
        scale = Math.min(scale, 20);
    }

    public E getEntity() {
        return entity;
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return age >= maxAge || fadeOutTicks == 0;
    }

    @Override
    public float getAlpha(float tickDelta) {
        float alpha = Math.min(1, MathHelper.sin(Math.min(age + tickDelta, maxAge - 2) / (maxAge - 2) * MathHelper.PI) * 18);
        if (fadeOutTicks >= 0) {
            alpha *= (fadeOutTicks + tickDelta) / 20F;
        }
        return alpha;
    }

    @Override
    public void update(float alpha) {
        super.update(alpha);

        if (alpha < MathHelper.EPSILON) {
            return;
        }

        entity.age++;
        entity.lastX = entity.lastRenderX = entity.getX();
        entity.lastY = entity.lastRenderY = entity.getY();
        entity.lastZ = entity.lastRenderZ = entity.getZ();

        entity.lastYaw = entity.getYaw();
        entity.lastPitch = entity.getPitch();

        if (entity instanceof LivingEntity living) {
            living.lastHeadYaw = living.headYaw;
        }

        animateEntity(entity);

        if (entity instanceof LivingEntity living) {
            living.updateLimbs(false);
        }

        entity.setVelocity(entity.getVelocity().add(
            (random.nextDouble() - 0.5D) / 10D,
            (random.nextDouble() - 0.5D) / 10D,
            (random.nextDouble() - 0.5D) / 10D
        ).multiply(0.99));
        entity.lookAt(EntityAnchor.FEET, entity.getEntityPos().add(entity.getVelocity()));

        if (fadeOutTicks > 0) {
            fadeOutTicks--;
        }

        if (fadeOutTicks < 0 && player.distanceTo(entity) < 4) {
            fadeOutTicks = 20;
        }
    }

    protected void animateEntity(E entity) {
        entity.move(MovementType.SELF, entity.getVelocity());
        entity.setYaw(MathHelper.wrapDegrees(entity.getYaw() + rotationYawPlus));
        if (entity instanceof LivingEntity l && l.canBreatheInWater()) {
           ((TouchingWaterAccessor)entity).setTouchingWater(true);
        }
    }

    @Override
    public Identifier getType() {
        return type;
    }
}
