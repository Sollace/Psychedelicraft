/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs.effects;

import java.util.Optional;

import ivorius.psychedelicraft.PSDamageSources;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import ivorius.psychedelicraft.entity.drugs.DrugType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class CocaineDrug extends SimpleDrug {
    static final Optional<Text> SLEEP_STATUS = Optional.of(Text.translatable("psychedelicraft.sleep.fail.coccaine"));

    public CocaineDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.COCAINE, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(LivingEntity entity, DrugProperties drugProperties) {
        super.update(entity, drugProperties);

        if (getActiveValue() > 0.0) {
            Random random = entity.world.random;
            int ticksExisted = drugProperties.ticksExisted;

            if (!entity.world.isClient) {
                double chance = (getActiveValue() - 0.8f) * 0.1f;

                if (ticksExisted % 20 == 0 && random.nextFloat() < chance) {
                    entity.damage(random.nextFloat() < 0.4f
                            ? PSDamageSources.STROKE
                            : random.nextFloat() < 0.5f
                            ? PSDamageSources.HEART_FAILURE
                            : PSDamageSources.RESPIRATORY_FAILURE, Integer.MAX_VALUE);
                }
            }
        }
    }

    @Override
    public float heartbeatVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4f, 1.0f), 0, 1) * 1.2f;
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.1f;
    }

    @Override
    public float breathVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4f, 1.0f), 0, 1) * 1.5F;
    }

    @Override
    public float breathSpeed() {
        return (float) getActiveValue() * 0.8f;
    }

    @Override
    public float randomJumpChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6f, 1.0f), 0, 1) * 0.03f;
    }

    @Override
    public float randomPunchChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.5f, 1.0f), 0, 1) * 0.02f;
    }

    @Override
    public float speedModifier() {
        return 1.0F + (float) getActiveValue() * 0.15F;
    }

    @Override
    public float digSpeedModifier() {
        return 1.0F + (float) getActiveValue() * 0.15F;
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return getActiveValue() > 0.4
                ? SLEEP_STATUS
                : Optional.empty();
    }

    @Override
    public float desaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.75f;
    }

    @Override
    public float handTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.6f, 1.0f), 0, 1);
    }

    @Override
    public float viewTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.8f, 1.0f), 0, 1);
    }

    @Override
    public float headMotionInertness() {
        return (float)getActiveValue() * 10.0f;
    }

    @Override
    public float bloomHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.0f, 0.6f), 0, 1) * 1.5f;
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3f, 0.7f, 1.0f), 0, 1) * 0.05f;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3f, 0.7f, 1.0f), 0, 1) * 0.05f;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3f, 0.7f, 1.0f), 0, 1) * 0.05f;
    }
}
