package ivorius.psychedelicraft.client.sound;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.mixin.client.SoundsAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.Sound.RegistrationType;
import net.minecraft.client.sound.SoundContainer;
import net.minecraft.util.Identifier;

/**
 * Created by Sollace on 11 June 2023
 */
public class ClientDrugMusicManager {
    private static final Identifier EMPTY_SOUND_ID = Psychedelicraft.id("drugs/generic");

    private WeakReference<MovingSoundDrug> activeSound = new WeakReference<>(null);

    public void update(DrugProperties properties) {
        if (!PsychedelicraftClient.getConfig().drugsBackgroundMusic.get()) {
            return;
        }
        MovingSoundDrug sound = getActiveSound();
        DrugType.REGISTRY
            .stream()
            .filter(type -> properties.getDrugValue(type) >= PsychedelicraftClient.getConfig().getBGMThreshold() && hasSoundsDefined(type.soundEvent().getId()))
            .sorted(Comparator.comparing(properties::getDrugValue).reversed())
            .findFirst()
            .filter(type -> sound == null || type != sound.getType())
            .ifPresent(drugType -> startPlayingSound(properties, drugType));
    }

    @Nullable
    private MovingSoundDrug getActiveSound() {
        MovingSoundDrug sound = activeSound.get();
        if (sound != null && sound.isDone()) {
            activeSound = new WeakReference<>(null);
            return null;
        }
        return sound;
    }

    private void startPlayingSound(DrugProperties properties, DrugType<?> type) {
        Psychedelicraft.LOGGER.info("Playing drug background music for " + type.id());
        MovingSoundDrug sound = getActiveSound();
        if (sound != null) {
            MinecraftClient.getInstance().getSoundManager().stop(sound);
            sound.markCompleted();
        }
        MovingSoundDrug newSound = new MovingSoundDrug(properties, type, sound == null ? 0.1F : sound.getVolume());
        MinecraftClient.getInstance().getSoundManager().play(newSound);
        activeSound = new WeakReference<>(newSound);
    }

    private boolean hasSoundsDefined(Identifier soundEventId) {
        return hasSoundsDefined(MinecraftClient.getInstance().getSoundManager().get(soundEventId));
    }

    private boolean hasSoundsDefined(SoundContainer<Sound> sound) {
        if (sound instanceof SoundsAccessor set) {
            for (var s : set.getSounds()) {
                if (hasSoundsDefined(s)) {
                    return true;
                }
            }
        }

        if (sound instanceof Sound s) {
            if (s.getRegistrationType() == RegistrationType.SOUND_EVENT) {
                return hasSoundsDefined(s.getIdentifier());
            }
            return !EMPTY_SOUND_ID.equals(s.getIdentifier());
        }

        return false;
    }
}
