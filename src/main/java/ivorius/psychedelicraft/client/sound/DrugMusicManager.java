package ivorius.psychedelicraft.client.sound;

import java.util.Optional;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class DrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<DrugType> activeDrug = Optional.empty();

    private Optional<MovingSoundDrug> activeSound = Optional.empty();

    private float volume;

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

    public void update() {
        if (activeDrug.isEmpty()) {
            activeDrug = DrugType.REGISTRY
                .stream()
                .filter(type -> PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(type) && properties.getDrugValue(type) >= PLAY_THRESHOLD)
                .findFirst()
                .map(this::startPlayingSound);
        }

        PlayerEntity entity = properties.asEntity();

        float destVolume = activeDrug
            .map(properties::getDrug)
            .map(drug -> MathHelper.lerp((float) drug.getActiveValue(), 0, 0.2F))
            .orElse(0F);

        if (destVolume >= PLAY_THRESHOLD) {
            volume = destVolume;
        } else {
            activeDrug = Optional.empty();
            activeSound.ifPresent(MovingSoundDrug::markCompleted);
            activeSound = Optional.empty();
            volume = 0;
        }


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

                delayUntilHeartbeat = speed <= 1 ? 1 : MathHelper.floor(35F / (speed - 1F));
                targetHeartbeatPulseStrength = 1;
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

    private DrugType startPlayingSound(DrugType type) {
        Registries.SOUND_EVENT.getOrEmpty(Psychedelicraft.id("drug." + type.id().getPath())).ifPresent(sound -> {
            SoundManager manager = MinecraftClient.getInstance().getSoundManager();
            activeSound.ifPresent(MovingSoundDrug::markCompleted);
            MovingSoundDrug newSound = new MovingSoundDrug(sound, SoundCategory.AMBIENT, this, type);
            activeSound = Optional.of(newSound);
            manager.play(newSound);
        });
        return type;
    }

    public float getVolumeFor(DrugType drugType) {
        return activeDrug.filter(drugType::equals).isPresent() ? volume : 0;
    }
}
