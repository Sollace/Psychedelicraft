/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.entity.player.PlayerEntity;
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
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getWorld().random;
            if (!entity.getWorld().isClient) {
                double chance = (getActiveValue() - 0.8F) * 0.1F;

                if (entity.age % 20 == 0 && random.nextFloat() < chance) {
                    entity.damage(drugProperties.damageOf(random.nextFloat() < 0.4F
                            ? PSDamageTypes.STROKE
                            : random.nextFloat() < 0.5F
                            ? PSDamageTypes.HEART_FAILURE
                            : PSDamageTypes.RESPIRATORY_FAILURE), Integer.MAX_VALUE);
                }
            }
        }
    }

    @Override
    public float heartbeatVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4F, 1) + (getTicksActive() * 0.0001F), 0, 1) * 1.2F;
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.1F + (getTicksActive() * 0.0001F);
    }

    @Override
    public float breathVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4f, 1.0f), 0, 1) * 1.5F;
    }

    @Override
    public float breathSpeed() {
        return (float) getActiveValue() * 0.8F;
    }

    @Override
    public float randomJumpChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1), 0, 1) * 0.03F;
    }

    @Override
    public float randomPunchChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.5F, 1), 0, 1) * 0.02F;
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
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.6F, 1), 0, 1);
    }

    @Override
    public float viewTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.8F, 1), 0, 1);
    }

    @Override
    public float headMotionInertness() {
        return (float)getActiveValue() * 10;
    }

    @Override
    public float bloomHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0, 0.6F), 0, 1) * 1.5F;
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.05F;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.05F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.05F;
    }
}
