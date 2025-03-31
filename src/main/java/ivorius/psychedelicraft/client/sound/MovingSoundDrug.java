package ivorius.psychedelicraft.client.sound;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends MovingSoundInstance {
    private DrugProperties properties;
    private DrugType<?> drugType;

    private float prevVolume;

    public MovingSoundDrug(DrugProperties properties, DrugType<?> drugType, float initialVolume) {
        super(drugType.soundEvent(), SoundCategory.AMBIENT, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.prevVolume = initialVolume;
        this.volume = initialVolume;
        this.relative = true;
        this.properties = properties;
        this.drugType = drugType;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    public void markCompleted() {
        setDone();
    }

    public DrugType<?> getType() {
        return drugType;
    }

    @Override
    public void tick() {
        prevVolume = volume;
        volume = MathUtils.approach(volume, getTargetVolume(), 0.1F);

        if (isDone()) {
            setDone();
        }
    }

    @Override
    public boolean isDone() {
        return super.isDone() || MathHelper.approximatelyEquals(volume, 0) || properties.asEntity().isRemoved() || MinecraftClient.getInstance().world == null;
    }

    @Override
    public final float getVolume() {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        return MathHelper.lerp(tickDelta, prevVolume, volume) * sound.getVolume().get(random);
    }

    private float getTargetVolume() {
        if (!PsychedelicraftClient.getConfig().drugsBackgroundMusic.get()) {
            return 0;
        }
        return MathHelper.clamp(properties.getDrugValue(drugType) - PsychedelicraftClient.getConfig().getBGMThreshold(), 0, 1);
    }
}
