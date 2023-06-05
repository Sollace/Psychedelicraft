/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.TouchingWaterAccessor;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.*;

public class EntityHallucination extends AbstractEntityHallucination {
    private float rotationYawPlus;

    public EntityHallucination(PlayerEntity player) {
        this(player, PSTags.Entities.SINGLE_ENTITY_HALLUCINATIONS);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityHallucination(PlayerEntity player, TagKey<EntityType<?>> entityTypes) {
        super(player, player.getWorld().getRegistryManager().get(RegistryKeys.ENTITY_TYPE)
                .getOrCreateEntryList(entityTypes)
                .getRandom(player.getWorld().random)
                .map(RegistryEntry::value)
                .orElse((EntityType)EntityType.PIG)
                .create(player.getWorld()));

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

    @Override
    protected void animateEntity() {
        entity.setPosition(entity.getPos().add(entity.getVelocity()));
        entity.setYaw(MathHelper.wrapDegrees(entity.getYaw() + rotationYawPlus));
        if (entity instanceof LivingEntity l && l.canBreatheInWater()) {
           ((TouchingWaterAccessor)entity).setTouchingWater(true);
        }
    }
}
