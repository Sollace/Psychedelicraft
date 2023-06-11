package ivorius.psychedelicraft.client.sound;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Sollace on 11 June 2023
 */
public class ClientDrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<DrugType> activeDrug = Optional.empty();

    private Optional<MovingSoundDrug> activeSound = Optional.empty();

    private float volume;

    public void update(DrugProperties properties) {
        if (activeDrug.isEmpty()) {
            activeDrug = DrugType.REGISTRY
                .stream()
                .filter(type -> PsychedelicraftClient.getConfig().audio.hasBackgroundMusic(type) && properties.getDrugValue(type) >= PLAY_THRESHOLD)
                .findFirst()
                .map(type -> startPlayingSound(properties, type));
        }

        float destVolume = activeDrug
            .map(properties::getDrug)
            .map(drug -> MathHelper.lerp((float) drug.getActiveValue(), 0, 0.2F))
            .orElse(0F);

        if (destVolume >= PLAY_THRESHOLD) {
            volume = destVolume;
        } else {
            activeDrug = Optional.empty();
            activeSound.ifPresent(MovingSoundDrug::markCompleted);
            activeSound = Optional.empty();
            volume = 0;
        }
    }

    private DrugType startPlayingSound(DrugProperties properties, DrugType type) {
        Psychedelicraft.LOGGER.info("Playing drug background music for " + type.id());
        SoundEvent sound = Registries.SOUND_EVENT.getOrEmpty(Psychedelicraft.id("drug." + type.id().getPath())).orElse(PSSounds.DRUG_GENERIC);
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();

        @Nullable
        WeightedSoundSet soundSet = manager.get(sound.getId());
        if (soundSet == null || soundSet.getSound(properties.asEntity().getRandom()) == null) {
            sound = PSSounds.DRUG_GENERIC;
        }

        if (sound == PSSounds.DRUG_GENERIC) {
            Psychedelicraft.LOGGER.info("Drug " + type.id() + " has no sound, using the generic version instead");
        }

        activeSound.ifPresent(MovingSoundDrug::markCompleted);
        MovingSoundDrug newSound = new MovingSoundDrug(sound, SoundCategory.AMBIENT, this, properties, type);
        activeSound = Optional.of(newSound);
        manager.play(newSound);
        return type;
    }

    public float getVolumeFor(DrugType drugType) {
        return activeDrug.filter(drugType::equals).isPresent() ? volume : 0;
    }
}
