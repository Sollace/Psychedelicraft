package ivorius.psychedelicraft.client.sound;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends MovingSoundInstance {
    private final ClientDrugMusicManager manager;
    private final DrugProperties properties;
    private final DrugType drugType;

    public MovingSoundDrug(SoundEvent event, SoundCategory category, ClientDrugMusicManager manager, DrugProperties properties, DrugType drugType) {
        super(event, category, Random.create());
        this.manager = manager;
        this.properties = properties;
        this.drugType = drugType;
    }

    public void markCompleted() {
        setDone();
    }

    @Override
    public void tick() {
        volume = manager.getVolumeFor(drugType);

        if (MathHelper.approximatelyEquals(volume, 0) || properties.asEntity().isRemoved()) {
            setDone();
            return;
        }

        x = (float) properties.asEntity().getX();
        y = (float) properties.asEntity().getY();
        z = (float) properties.asEntity().getZ();
    }
}
