package ivorius.psychedelicraft.client.sound;

import java.util.Comparator;
import java.util.Optional;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Created by Sollace on 11 June 2023
 */
public class ClientDrugMusicManager {
    public static final float PLAY_THRESHOLD = 0.01F;

    private Optional<MovingSoundDrug> activeSound = Optional.empty();

    public void update(DrugProperties properties) {
        DrugType<?> activeDrug = getActiveSound().map(MovingSoundDrug::getType).orElse(null);
        Comparator<DrugType<?>> comparator = Comparator.comparing(type -> properties.getDrugValue(type));
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

    private MovingSoundDrug startPlayingSound(DrugProperties properties, DrugType<?> type) {
        Psychedelicraft.LOGGER.info("Playing drug background music for " + type.id());
        SoundEvent sound = type.soundEvent();
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        activeSound.ifPresent(MovingSoundDrug::markCompleted);
        MovingSoundDrug newSound = new MovingSoundDrug(sound, SoundCategory.AMBIENT, properties, type);
        manager.play(newSound);
        return newSound;
    }
}
