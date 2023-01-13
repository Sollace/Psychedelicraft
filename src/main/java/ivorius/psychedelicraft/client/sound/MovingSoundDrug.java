package ivorius.psychedelicraft.client.sound;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import ivorius.psychedelicraft.entity.drugs.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends MovingSoundInstance {

    // TODO: Need to restart ambient sounds when the player joins a world/moves between dimensions
    public static void initializeForEntity(DrugProperties drugProperties) {
        SoundManager soundHandler = MinecraftClient.getInstance().getSoundManager();
        for (DrugType type : drugProperties.getAllDrugNames()) {
            if (PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(type)) {
                Registries.SOUND_EVENT.getOrEmpty(Psychedelicraft.id("drug." + type.id().getPath())).ifPresent(sound -> {
                    soundHandler.play(new MovingSoundDrug(sound, SoundCategory.AMBIENT, drugProperties.asEntity(), drugProperties, type));
                });
            }
        }
    }

    private final Entity entity;
    private final DrugProperties drugProperties;
    private final DrugType drugType;

    public MovingSoundDrug(SoundEvent event, SoundCategory category, Entity entity, DrugProperties drugProperties, DrugType drugType) {
        super(event, category, Random.create());
        this.entity = entity;
        this.drugProperties = drugProperties;
        this.drugType = drugType;
    }

    @Override
    public void tick() {
        if (entity.isRemoved()) {
            setDone();
        } else {
            x = (float) entity.getX();
            y = (float) entity.getY();
            z = (float) entity.getZ();
            volume = drugProperties.getMusicManager().getVolumeFor(drugType);
        }
    }
}
