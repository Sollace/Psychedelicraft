package ivorius.psychedelicraft.client.sound;

import java.util.Optional;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import ivorius.psychedelicraft.entity.drugs.DrugType;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 */
public class DrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<DrugType> activeDrug = Optional.empty();
    private float volume;

    public void update(DrugProperties properties) {
        if (activeDrug.isEmpty()) {
            activeDrug = properties.getAllDrugNames().stream()
                .filter(type -> PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(type) && properties.getDrugValue(type) >= PLAY_THRESHOLD)
                .findFirst();
        }

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
    }

    public float getVolumeFor(DrugType drugType) {
        return activeDrug.filter(drugType::equals).isPresent() ? volume : 0;
    }
}
