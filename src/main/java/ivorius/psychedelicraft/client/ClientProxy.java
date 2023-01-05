/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.PSProxy;
import ivorius.psychedelicraft.client.rendering.DrugRenderer;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.config.Configuration;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugFactory;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.entities.drugs.DrugRegistry;
import java.util.ArrayList;
import java.util.List;

public class ClientProxy extends PSProxy
{
    public static float dofFocalPointNear;
    public static float dofFocalBlurNear;
    public static float dofFocalPointFar;
    public static float dofFocalBlurFar;

    public static float pauseMenuBlur;

    @Override
    public void createDrugRenderer(DrugProperties drugProperties) {
        drugProperties.renderer = new DrugRenderer();
    }

    @Override
    public void loadConfig(Configuration config, String configID)
    {
        if (configID == null || configID.equals(PSConfig.CATEGORY_BALANCING))
        {
            PSConfig.distortIncomingMessages = config.get("distortIncomingMessages", PSConfig.CATEGORY_BALANCING, true, "Whether the mod should distort received chat messages when drugs have been consumed ('confusion').");
        }

        if (configID == null || configID.equals(PSConfig.CATEGORY_VISUAL))
        {
            PSRenderStates.sunFlareIntensity = config.get(PSConfig.CATEGORY_VISUAL, "sunFlareIntensity", 0.25F);
            PSRenderStates.doHeatDistortion = config.get(PSConfig.CATEGORY_VISUAL, "biomeHeatDistortion", true);
            PSRenderStates.doWaterDistortion = config.get(PSConfig.CATEGORY_VISUAL, "waterDistortion", true);
            PSRenderStates.doMotionBlur = config.get(PSConfig.CATEGORY_VISUAL, "motionBlur", true);
//        DrugShaderHelper.doShadows = config.get(PSConfig.CATEGORY_VISUAL, "doShadows", true);

            dofFocalPointNear = config.get(PSConfig.CATEGORY_VISUAL, "dofFocalPointNear", 0.2f, "The point at which DoF starts blurring the screen, towards the player, in blocks. (Usually 0 to 1)");
            dofFocalPointFar = config.get(PSConfig.CATEGORY_VISUAL, "dofFocalPointFar", 128f, "The point at which DoF starts blurring the screen, away from the player, in blocks. (Usually about 128)");
            dofFocalBlurNear = config.get(PSConfig.CATEGORY_VISUAL, "dofFocalBlurNear", 0f, "The strength of DoF blur towards the player. (Usually 0 to 1)");
            dofFocalBlurFar = config.get(PSConfig.CATEGORY_VISUAL, "dofFocalBlurFar", 0f, "The strength of DoF blur away from the player. (Usually 0 to 1)");

            DrugProperties.waterOverlayEnabled = config.get(PSConfig.CATEGORY_VISUAL, "waterOverlayEnabled", true);
            DrugProperties.hurtOverlayEnabled = config.get(PSConfig.CATEGORY_VISUAL, "hurtOverlayEnabled", true);
            DrugProperties.digitalEffectPixelRescale = new float[]{config.get(PSConfig.CATEGORY_VISUAL, "digitalEffectPixelRescaleX", 0.05F),
                    config.get(PSConfig.CATEGORY_VISUAL, "digitalEffectPixelRescaleY", 0.05F)};

            pauseMenuBlur = config.get(PSConfig.CATEGORY_VISUAL, "pauseMenuBlur", 0f, "Amount of blur that should be applied to the game screen on pause.");
        }

        if (configID == null || configID.equals(PSConfig.CATEGORY_AUDIO))
        {
            for (String s : DrugRegistry.getAllDrugNames())
                PSConfig.readHasBGM(s, config);
        }
    }
}
