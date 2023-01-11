/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

import java.util.Optional;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 01.11.14.
 */
public class CaffeineDrug extends SimpleDrug {
    static final Optional<Text> SLEEP_STATUS = Optional.of(Text.translatable("psychedelicraft.sleep.fail.caffiene"));

    public CaffeineDrug(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float heartbeatVolume() {
        return MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1);
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.2f;
    }

    @Override
    public float breathVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4F, 1), 0, 1) * 0.5f;
    }

    @Override
    public float breathSpeed() {
        return (float) getActiveValue() * 0.3f;
    }

    @Override
    public float randomJumpChance() {
        return MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1) * 0.07f;
    }

    @Override
    public float randomPunchChance() {
        return MathHelper.getLerpProgress((float) getActiveValue(), 0.3F, 1) * 0.05f;
    }

    @Override
    public float speedModifier() {
        return 1 + (float) getActiveValue() * 0.2F;
    }

    @Override
    public float digSpeedModifier() {
        return 1 + (float) getActiveValue() * 0.2F;
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return getActiveValue() > 0.1
                ? SLEEP_STATUS
                : Optional.empty();
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.3f;
    }

    @Override
    public float handTrembleStrength() {
        return MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1.0F);
    }

    @Override
    public float viewTrembleStrength() {
        return MathHelper.getLerpProgress((float) getActiveValue(), 0.8F, 1.0F);
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.03F;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.03F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1) * 0.05F;
    }
}
