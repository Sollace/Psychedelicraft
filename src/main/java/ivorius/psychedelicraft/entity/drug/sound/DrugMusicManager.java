package ivorius.psychedelicraft.entity.drug.sound;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class DrugMusicManager implements NbtSerialisable {
    public static final float PLAY_THRESHOLD = 0.01F;

    final DrugProperties properties;

    private int delayUntilHeartbeat;
    private int delayUntilBreath;
    private boolean lastBreathWasIn;

    private float prevHeartbeatPulseStrength;
    private float heartbeatPulseStrength;
    private float targetHeartbeatPulseStrength;

    public DrugMusicManager(DrugProperties properties) {
        this.properties = properties;
    }

    public void copyFrom(DrugMusicManager old, boolean alive) {
        if (alive) {
            delayUntilHeartbeat = old.delayUntilHeartbeat;
            delayUntilBreath = old.delayUntilBreath;
            lastBreathWasIn = old.lastBreathWasIn;

            prevHeartbeatPulseStrength = old.prevHeartbeatPulseStrength;
            heartbeatPulseStrength = old.heartbeatPulseStrength;
            targetHeartbeatPulseStrength = old.targetHeartbeatPulseStrength;
        }
    }

    public void update() {
        PlayerEntity entity = properties.asEntity();

        if (delayUntilHeartbeat > 0) {
            delayUntilHeartbeat--;
        }

        if (delayUntilBreath > 0) {
            delayUntilBreath--;
        }

        prevHeartbeatPulseStrength = heartbeatPulseStrength;
        heartbeatPulseStrength = MathUtils.approach(heartbeatPulseStrength, targetHeartbeatPulseStrength, 0.2F);
        if (targetHeartbeatPulseStrength > 0) {
            targetHeartbeatPulseStrength -= 0.02F;
        }

        if (delayUntilHeartbeat == 0) {
            float heartbeatVolume = properties.getModifier(Drug.HEART_BEAT_VOLUME);
            if (heartbeatVolume > 0) {
                float speed = properties.getModifier(Drug.HEART_BEAT_SPEED);
                delayUntilHeartbeat = speed <= 1 ? -MathHelper.floor(35 * (speed - 1F)) : MathHelper.floor(35F / (speed - 1F));
                targetHeartbeatPulseStrength = 2 - properties.getModifier(Drug.PAIN_SUPPRESSION);
                entity.getWorld().playSound(entity.getX(), entity.getY(), entity.getZ(),
                        PSSounds.ENTITY_PLAYER_HEARTBEAT,
                        SoundCategory.AMBIENT, heartbeatVolume, speed, false);
            }
        }

        if (delayUntilBreath == 0) {
            lastBreathWasIn = !lastBreathWasIn;

            float breathVolume = properties.getModifier(Drug.BREATH_VOLUME);
            if (breathVolume > 0) {
                float speed = properties.getModifier(Drug.BREATH_SPEED);
                delayUntilBreath = MathHelper.floor(30F / speed);
                entity.getWorld().playSoundFromEntity(entity, entity, PSSounds.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS,
                        breathVolume,
                        speed * 0.05F + 0.9F + (lastBreathWasIn ? 0.15F : 0)
                );
            }
        }
    }

    public float getHeartbeatPulseStrength(float delta) {
        return MathHelper.lerp(delta, prevHeartbeatPulseStrength, heartbeatPulseStrength);
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.putInt("delayUntilHeartbeat", delayUntilHeartbeat);
        compound.putInt("delayUntilBreath", delayUntilBreath);
        compound.putBoolean("lastBreathWasIn", lastBreathWasIn);
        compound.putFloat("heartbeatPulseStrength", heartbeatPulseStrength);
        compound.putFloat("targetHeartbeatPulseStrength", targetHeartbeatPulseStrength);
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        delayUntilHeartbeat = compound.getInt("delayUntilHeartbeat");
        delayUntilBreath = compound.getInt("delayUntilBreath");
        lastBreathWasIn = compound.getBoolean("lastBreathWasIn");
        heartbeatPulseStrength = compound.getFloat("heartbeatPulseStrength");
        targetHeartbeatPulseStrength = compound.getFloat("targetHeartbeatPulseStrength");
    }
}
