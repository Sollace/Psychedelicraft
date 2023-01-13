package ivorius.psychedelicraft.client.sound;

import java.util.Locale;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends MovingSoundInstance {

    // TODO: Need to restart ambient sounds when the player joins a world/moves between dimensions
    public static void initializeForEntity(DrugProperties drugProperties) {
        SoundManager soundHandler = MinecraftClient.getInstance().getSoundManager();
        for (String drugName : drugProperties.getAllDrugNames()) {
            if (PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(drugName)) {
                Registries.SOUND_EVENT.getOrEmpty(Psychedelicraft.id("drug." + drugName.toLowerCase(Locale.ROOT))).ifPresent(sound -> {
                    soundHandler.play(new MovingSoundDrug(sound, SoundCategory.AMBIENT, drugProperties.asEntity(), drugProperties, drugName));
                });
            }
        }
    }

    private final Entity entity;
    private final DrugProperties drugProperties;
    private final String drugName;

    public MovingSoundDrug(SoundEvent event, SoundCategory category, Entity entity, DrugProperties drugProperties, String drugName) {
        super(event, category, Random.create());
        this.entity = entity;
        this.drugProperties = drugProperties;
        this.drugName = drugName;
    }

    @Override
    public void tick() {
        if (entity.isRemoved()) {
            setDone();
        } else {
            x = (float) entity.getX();
            y = (float) entity.getY();
            z = (float) entity.getZ();
            volume = drugProperties.musicManager.getVolumeFor(drugName);
        }
    }
}
