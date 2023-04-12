/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 01.11.14.
 */
public class CaffeineDrug extends SimpleDrug {
    static final Optional<Text> SLEEP_STATUS = Optional.of(Text.translatable("psychedelicraft.sleep.fail.insomnia"));

    private final float breathVolumeMultiplier;

    public CaffeineDrug(double decSpeed, double decSpeedPlus, float breathVolumeMultiplier) {
        super(DrugType.CAFFEINE, decSpeed, decSpeedPlus);
        this.breathVolumeMultiplier = breathVolumeMultiplier;
    }

    @Override
    public float heartbeatVolume() {
        return breathVolumeMultiplier * MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1), 0, 1);
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.2f;
    }

    @Override
    public float breathVolume() {
        return breathVolumeMultiplier * MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4F, 1), 0, 1) * 0.5F;
    }

    @Override
    public float breathSpeed() {
        return (float) getActiveValue() * 0.3F;
    }

    @Override
    public float randomJumpChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1), 0, 1) * 0.07F;
    }

    @Override
    public float randomPunchChance() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.3F, 1), 0, 1) * 0.05F;
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
        return (float)getActiveValue() * 0.3F;
    }

    @Override
    public float handTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1), 0, 1);
    }

    @Override
    public float viewTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.8F, 1), 0, 1);
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.03F;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.03F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.7F, 1), 0, 1) * 0.05F;
    }
}
