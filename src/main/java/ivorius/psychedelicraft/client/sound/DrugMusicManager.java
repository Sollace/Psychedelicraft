package ivorius.psychedelicraft.client.sound;

import java.util.Optional;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 */
public class DrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<DrugType> activeDrug = Optional.empty();
    private float volume;

    private final DrugProperties properties;

    private int delayUntilHeartbeat;
    private int delayUntilBreath;
    private boolean lastBreathWasIn;

    public DrugMusicManager(DrugProperties properties) {
        this.properties = properties;
    }

    public void update() {
        if (activeDrug.isEmpty()) {
            activeDrug = DrugType.REGISTRY
                .stream()
                .filter(type -> PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(type) && properties.getDrugValue(type) >= PLAY_THRESHOLD)
                .findFirst();
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
            volume = 0;
        }


        if (delayUntilHeartbeat > 0) {
            delayUntilHeartbeat--;
        }

        if (delayUntilBreath > 0) {
            delayUntilBreath--;
        }

        if (delayUntilHeartbeat == 0) {
            float heartbeatVolume = properties.getModifier(Drug.HEART_BEAT_VOLUME);
            if (heartbeatVolume > 0) {
                float speed = properties.getModifier(Drug.HEART_BEAT_SPEED);

                delayUntilHeartbeat = MathHelper.floor(35.0f / (speed - 1.0f));
                entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(),
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

                // TODO: (Sollace) Breathing sounds like the thing from the black lagoon
                entity.sendMessage(Text.literal(breathVolume + ""));
                entity.world.playSoundFromEntity(entity, entity, PSSounds.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS,
                        breathVolume,
                        speed * 0.05F + 0.9F + (lastBreathWasIn ? 0.15F : 0)
                );
            }
        }
    }

    public float getVolumeFor(DrugType drugType) {
        return activeDrug.filter(drugType::equals).isPresent() ? volume : 0;
    }
}
