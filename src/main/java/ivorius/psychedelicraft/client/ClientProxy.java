/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.joml.Vector2f;

import ivorius.psychedelicraft.PSProxy;
import ivorius.psychedelicraft.client.rendering.DrugRenderer;
import ivorius.psychedelicraft.config.*;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;

public class ClientProxy extends PSProxy {
    @Override
    public void createDrugRenderer(DrugProperties drugProperties) {
        drugProperties.renderer = new DrugRenderer();
    }

    @Override
    public PSConfig createConfig() {
        return new Config();
    }

    public static class Config extends PSConfig {
        public final Audio audio = new Audio();
        public final Visual visual = new Visual();

        public static class Audio {
            public String[] drugsWithBackgroundMusic = new String[0];
            private transient Set<String> drugsWithBackgroundMusicSet;

            public boolean hasBackgroundMusic(String drugName) {
                if (drugsWithBackgroundMusicSet == null) {
                    drugsWithBackgroundMusicSet = Arrays.stream(drugsWithBackgroundMusic == null ? new String[0] : drugsWithBackgroundMusic)
                            .distinct()
                            .collect(Collectors.toSet());
                }
                return drugsWithBackgroundMusicSet.contains(drugName);
            }
        }

        public static class Visual {
            public final float dofFocalPointNear = 0.2F;
            public final float dofFocalBlurNear = 0;
            public final float dofFocalPointFar = 128;
            public final float dofFocalBlurFar = 0;

            public final float pauseMenuBlur = 0;

            public final boolean shader2DEnabled = true;
            public final boolean shader3DEnabled = true;
            // (Sollace) made transient because this config was disabled before
            public transient final boolean doShadows = false;

            public final boolean doHeatDistortion = true;
            public final boolean doWaterDistortion = true;
            public final boolean doMotionBlur = true;

            public final float sunFlareIntensity = 0.25F;
            public final int shadowPixelsPerChunk = 256;

            public final boolean waterOverlayEnabled = true;
            public final boolean hurtOverlayEnabled = true;
            public final Vector2f digitalEffectPixelRescale = new Vector2f(0.05F, 0.05F);

            private transient float[] digitalEffectPixelRescaleF;

            public float[] getDigitalEffectPixelResize() {
                if (digitalEffectPixelRescaleF == null) {
                    digitalEffectPixelRescaleF = new float[] { digitalEffectPixelRescale.x, digitalEffectPixelRescale.y };
                }
                return digitalEffectPixelRescaleF;
            }
        }
    }
}
