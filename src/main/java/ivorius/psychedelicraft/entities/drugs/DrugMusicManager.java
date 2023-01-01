package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.config.PSConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 */
public class DrugMusicManager
{
    public static final float PLAY_THRESHOLD = 0.01f;

    private String activeDrug;
    private float volume;

    public void update(LivingEntity entity, DrugProperties drugProperties)
    {
        if (activeDrug == null)
        {
            for (String drugName : drugProperties.getAllDrugNames())
            {
                if (PSConfig.hasBGM(drugName) && drugProperties.getDrugValue(drugName) >= PLAY_THRESHOLD)
                    activeDrug = drugName;
            }
        }

        float destVolume = 0.0f;

        if (activeDrug != null)
        {
            Drug drug = drugProperties.getDrug(activeDrug);
            if (drug != null)
                destVolume = MathHelper.lerp((float) drug.getActiveValue(), 0.0f, 0.2f);
        }

        if ((double) destVolume >= PLAY_THRESHOLD)
        {
            this.volume = destVolume;
        }
        else
        {
            activeDrug = null;
            this.volume = 0.0F;
        }
    }

    public String getActiveDrug()
    {
        return activeDrug;
    }

    public float getVolume()
    {
        return volume;
    }
}
