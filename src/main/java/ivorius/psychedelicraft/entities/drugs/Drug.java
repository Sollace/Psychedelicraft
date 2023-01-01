/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public interface Drug
{
    void update(LivingEntity entity, DrugProperties drugProperties);

    void reset(LivingEntity entity, DrugProperties drugProperties);

    void writeToNBT(NbtCompound compound);

    void readFromNBT(NbtCompound compound);

    double getActiveValue();

    void addToDesiredValue(double effect);

    void setDesiredValue(double effect);

    boolean isVisible();

    void setLocked(boolean drugLocked);

    boolean isLocked();

    float heartbeatVolume();

    float heartbeatSpeed();

    float breathVolume();

    float breathSpeed();

    float randomJumpChance();

    float randomPunchChance();

    float digSpeedModifier();

    float speedModifier();

    float soundVolumeModifier();

    //EntityPlayer.EnumStatus getSleepStatus();

    void applyContrastColorization(float[] rgba);

    void applyColorBloom(float[] rgba);

    float desaturationHallucinationStrength();

    float superSaturationHallucinationStrength();

    float contextualHallucinationStrength();

    float colorHallucinationStrength();

    float movementHallucinationStrength();

    float handTrembleStrength();

    float viewTrembleStrength();

    float headMotionInertness();

    float bloomHallucinationStrength();

    float viewWobblyness();

    float doubleVision();

    float motionBlur();

    @Environment(EnvType.CLIENT)
    void drawOverlays(float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);
}
