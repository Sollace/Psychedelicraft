package ivorius.psychedelicraft.client;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.joml.Vector2f;

import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.util.Identifier;

public class PSClientConfig {
    public PSClientConfig.Audio audio = new Audio();
    public PSClientConfig.Visual visual = new Visual();

    public static class Audio {
        public String[] drugsWithBackgroundMusic = DrugType.REGISTRY.getIds().stream().map(Identifier::toString).toArray(String[]::new);
        private transient Set<String> drugsWithBackgroundMusicSet;

        private Set<String> loadMusicSet() {
            if (drugsWithBackgroundMusicSet == null) {
                drugsWithBackgroundMusicSet = Arrays.stream(drugsWithBackgroundMusic == null ? new String[0] : drugsWithBackgroundMusic)
                        .distinct()
                        .collect(Collectors.toSet());
            }
            return drugsWithBackgroundMusicSet;
        }

        public boolean hasBackgroundMusic(DrugType<?> drugType) {
            return loadMusicSet().contains(drugType.id().toString());
        }

        public boolean setHasBackgroundMusic(DrugType<?> drugType, boolean value) {
            Set<String> musicSet = loadMusicSet();
            if (value) {
                musicSet.add(drugType.id().toString());
            } else {
                musicSet.remove(drugType.id().toString());
            }
            drugsWithBackgroundMusic = musicSet.toArray(String[]::new);
            return value;
        }
    }

    public static class Visual {
        public float dofFocalPointNear = 0.2F;
        public float dofFocalBlurNear = 0;
        public float dofFocalPointFar = 128;
        public float dofFocalBlurFar = 0;

        public boolean shader2DEnabled = true;
        public boolean shader3DEnabled = true;
        // (Sollace) made transient because this config was disabled before
        public transient boolean doShadows = false;

        public boolean doHeatDistortion = true;
        public boolean doWaterDistortion = true;
        public boolean doMotionBlur = true;

        public float sunFlareIntensity = 0.25F;
        public int shadowPixelsPerChunk = 256;

        public boolean waterOverlayEnabled = true;
        public boolean hurtOverlayEnabled = true;
        public Vector2f digitalEffectPixelRescale = new Vector2f(0.05F, 0.05F);

        private transient float[] digitalEffectPixelRescaleF;

        public float[] getDigitalEffectPixelResize() {
            if (digitalEffectPixelRescaleF == null) {
                digitalEffectPixelRescaleF = new float[] { digitalEffectPixelRescale.x, digitalEffectPixelRescale.y };
            }
            return digitalEffectPixelRescaleF;
        }
    }
}