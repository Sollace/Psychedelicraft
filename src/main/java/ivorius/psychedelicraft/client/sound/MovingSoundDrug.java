package ivorius.psychedelicraft.client.sound;

import java.util.Optional;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends MovingSoundInstance {
    private final DrugProperties properties;
    private final DrugType drugType;

    public MovingSoundDrug(SoundEvent event, SoundCategory category, DrugProperties properties, DrugType drugType) {
        super(event, category, Random.create());
        this.properties = properties;
        this.drugType = drugType;
        this.repeat = true;
    }

    public void markCompleted() {
        setDone();
    }

    public DrugType getType() {
        return drugType;
    }

    @Override
    public void tick() {
        volume = getTargetVolume();

        if (MathHelper.approximatelyEquals(volume, 0) || properties.asEntity().isRemoved()) {
            setDone();
            return;
        }

        x = (float) properties.asEntity().getX();
        y = (float) properties.asEntity().getY();
        z = (float) properties.asEntity().getZ();
    }

    private float getTargetVolume() {
        double activeValue = Optional.of(drugType)
                .map(properties::getDrug)
                .filter(drug -> drug.getType() == drugType)
                .map(Drug::getActiveValue)
                .orElse(0D);
        if (activeValue <= ClientDrugMusicManager.PLAY_THRESHOLD) {
            return 0;
        }
        return MathUtils.inverseLerp(MathHelper.clamp((float)activeValue, 0, 1), 0, 0.4F);
    }
}
