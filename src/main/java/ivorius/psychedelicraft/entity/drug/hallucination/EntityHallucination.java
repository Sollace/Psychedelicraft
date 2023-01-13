/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class EntityHallucination extends AbstractEntityHallucination {
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

    public float rotationYawPlus;

    public EntityHallucination(PlayerEntity player) {
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

        color = new float[] {
            rand.nextFloat(),
            rand.nextFloat(),
            rand.nextFloat()
        };

        scale = 1;
        while (rand.nextFloat() < 0.3F) {
            scale *= rand.nextFloat() * 2.7f + 0.3F;
        }
        scale = Math.min(scale, 20);
    }

    @Override
    protected void animateEntity() {
        entity.setPosition(entity.getPos().add(entity.getVelocity()));
        entity.setYaw(MathHelper.wrapDegrees(entity.getYaw() + rotationYawPlus));
    }

    @Override
    public void receiveChatMessage(String message, LivingEntity entity) { }

}
