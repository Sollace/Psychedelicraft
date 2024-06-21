/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class AlcoholDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(VIEW_WOBBLYNESS, 0.5F)
            .put(DOUBLE_VISION, f -> MathUtils.project(f, 0.25F, 1))
            .put(MOTION_BLUR, f -> MathUtils.project(f, 0.5F, 1) * 0.3F)
            .build();

    public AlcoholDrug(DrugType type, double decSpeed, double decSpeedPlus) {
        super(type, decSpeed, decSpeedPlus);
    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, Random random) {
        PlayerEntity entity = properties.asEntity();

        double activeValue = getActiveValue();

        if ((getTicksActive() % 20) == 0 && random.nextFloat() < (activeValue - 0.9F) * 2) {
            entity.damage(PSDamageTypes.create(entity.getWorld(), PSDamageTypes.ALCOHOL_POSIONING), (int) ((activeValue - 0.9F) * 50 + 4));
            if (entity.isDead()) {
                return true;
            }
        }

        return super.tickSideEffects(properties, random);
    }

    @Override
    protected void tickClientEffects(DrugProperties properties, Random random) {
        PlayerEntity entity = properties.asEntity();
        double activeValue = getActiveValue();
        double motionEffect = Math.min(activeValue, 0.8) + (MathHelper.clamp(entity.getVelocity().length(), 0, 1) * 3) * activeValue;
        rotateEntityPitch(entity, MathHelper.sin(entity.age / 600F * MathHelper.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, MathHelper.cos(entity.age / 500F * MathHelper.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

        rotateEntityPitch(entity, MathHelper.sin(entity.age / 180F * MathHelper.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, MathHelper.cos(entity.age / 150F * MathHelper.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        double value = getActiveValue();
        super.onWakeUp(drugProperties);

        if (value > 0) {
            PlayerEntity player = drugProperties.asEntity();
            Random random = player.getWorld().random;

            if (random.nextFloat() > (1 - value)) {
                player.animateDamage(random.nextFloat() * MathHelper.TAU);
                player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1, 1);
                drugProperties.addToDrug(DrugType.SLEEP_DEPRIVATION, 0.25F);
                PSCriteria.HANGOVER.trigger(player);
            }
        }
    }
}
