package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 */
public class DrugMusicManager {
    // TODO: (Sollace) Move client code to the client package
    public static final float PLAY_THRESHOLD = 0.01f;

    private String activeDrug;
    private float volume;

    public void update(LivingEntity entity, DrugProperties drugProperties) {
        if (activeDrug == null) {
            for (String drugName : drugProperties.getAllDrugNames()) {
                if (PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(drugName) && drugProperties.getDrugValue(drugName) >= PLAY_THRESHOLD) {
                    activeDrug = drugName;
                }
            }
        }

        float destVolume = 0;

        if (activeDrug != null) {
            Drug drug = drugProperties.getDrug(activeDrug);
            if (drug != null) {
                destVolume = MathHelper.lerp((float) drug.getActiveValue(), 0.0f, 0.2f);
            }
        }

        if ((double) destVolume >= PLAY_THRESHOLD) {
            volume = destVolume;
        } else {
            activeDrug = null;
            volume = 0;
        }
    }

    public String getActiveDrug() {
        return activeDrug;
    }

    public float getVolume() {
        return volume;
    }
}
