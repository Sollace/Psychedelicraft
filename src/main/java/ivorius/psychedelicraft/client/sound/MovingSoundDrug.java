package ivorius.psychedelicraft.client.sound;

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
    private final DrugMusicManager manager;
    private final DrugType drugType;

    public MovingSoundDrug(SoundEvent event, SoundCategory category, DrugMusicManager manager, DrugType drugType) {
        super(event, category, Random.create());
        this.manager = manager;
        this.drugType = drugType;
    }

    public void markCompleted() {
        setDone();
    }

    @Override
    public void tick() {
        volume = manager.getVolumeFor(drugType);

        if (MathHelper.approximatelyEquals(volume, 0) || manager.properties.asEntity().isRemoved()) {
            setDone();
            return;
        }

        x = (float) manager.properties.asEntity().getX();
        y = (float) manager.properties.asEntity().getY();
        z = (float) manager.properties.asEntity().getZ();
    }
}
