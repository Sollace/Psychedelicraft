package ivorius.psychedelicraft.client.sound;

import java.util.Comparator;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Created by Sollace on 11 June 2023
 */
public class ClientDrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<MovingSoundDrug> activeSound = Optional.empty();

    public void update(DrugProperties properties) {
        DrugType activeDrug = getActiveSound().map(MovingSoundDrug::getType).orElse(null);
        Comparator<DrugType> comparator = Comparator.comparing(type -> properties.getDrugValue(type));
        DrugType.REGISTRY
            .stream()
            .filter(PsychedelicraftClient.getConfig().audio::hasBackgroundMusic)
            .filter(type -> properties.getDrugValue(type) >= PLAY_THRESHOLD)
            .sorted(comparator.reversed())
            .findFirst()
            .filter(type -> type != activeDrug)
            .ifPresent(drugType -> activeSound = Optional.ofNullable(startPlayingSound(properties, drugType)));
    }

    private Optional<MovingSoundDrug> getActiveSound() {
        if (activeSound.isPresent()) {
            activeSound = activeSound.filter(sound -> !sound.isDone());
        }
        return activeSound;
    }

    private MovingSoundDrug startPlayingSound(DrugProperties properties, DrugType type) {
        Psychedelicraft.LOGGER.info("Playing drug background music for " + type.id());
        SoundEvent sound = Registry.SOUND_EVENT.getOrEmpty(Psychedelicraft.id("drug." + type.id().getPath())).orElse(PSSounds.DRUG_GENERIC);
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();

        @Nullable
        WeightedSoundSet soundSet = manager.get(sound.getId());
        if (soundSet == null || soundSet.getSound(properties.asEntity().getRandom()) == null) {
            sound = PSSounds.DRUG_GENERIC;
        }

        if (sound == PSSounds.DRUG_GENERIC) {
            Psychedelicraft.LOGGER.info("Drug " + type.id() + " has no sound, using the generic version instead");
        }

        MovingSoundDrug newSound = new MovingSoundDrug(sound, SoundCategory.AMBIENT, properties, type);
        activeSound.ifPresent(MovingSoundDrug::markCompleted);
        manager.play(newSound);
        return newSound;
    }
}
