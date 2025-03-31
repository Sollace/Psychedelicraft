package ivorius.psychedelicraft.client;

import java.nio.file.Path;
import org.joml.Vector2f;

import com.google.gson.GsonBuilder;
import com.minelittlepony.common.util.registry.RegistryTypeAdapter;
import com.minelittlepony.common.util.settings.Config;
import com.minelittlepony.common.util.settings.HeirarchicalJsonConfigAdapter;
import com.minelittlepony.common.util.settings.Setting;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.util.math.MathHelper;

public class PSClientConfig extends Config {
    public final Setting<Float> dofFocalPointNear = value("visual", "dofFocalPointNear", 0.2F)
            .addComment("Sets the distance from the camera of the near focal point when doing depth of field")
            .addComment("Default: 0.2");
    public final Setting<Float> dofFocalBlurNear = value("visual", "dofFocalBlurNear", 0F)
            .addComment("Sets blurring amount at the near focal point")
            .addComment("Default: 0");
    public final Setting<Float> dofFocalPointFar = value("visual", "dofFocalPointFar", 128F)
            .addComment("Sets the distance from the camera of the far focal point when doing depth of field")
            .addComment("Default: 128 (up to render distance)");
    public final Setting<Float> dofFocalBlurFar = value("visual", "dofFocalBlurFar", 0F)
            .addComment("Sets blurring amount at the far focal point")
            .addComment("Default: 0");

    public final Setting<Boolean> shader2DEnabled = value("visual", "shader2DEnabled", true)
            .addComment("Enables and disables all 2D (screen) shader effects.")
            .addComment("e.g. Blurring, double vision, sunflare, etc")
            .addComment("Default: true");
    public final Setting<Boolean> shader3DEnabled = value("visual", "shader3DEnabled", true)
            .addComment("Enables and disables all 3D (geometric) shader effects.")
            .addComment("e.g. Waves")
            .addComment("Default: true");

    public final Setting<Boolean> doHeatDistortion = value("visual", "doHeatDistortion", true)
            .addComment("Sets whether to apply a distortion effect when the player is in arid environments")
            .addComment("Default: true");
    public final Setting<Boolean> doWaterDistortion = value("visual", "doWaterDistortion", true)
            .addComment("Sets whether to apply a distortion effect when the player is submerged")
            .addComment("Default: true");
    public final Setting<Boolean> doMotionBlur = value("visual", "doMotionBlur", true)
            .addComment("Sets whether drug effects are allowed can cause motion blur effects")
            .addComment("Default: true");

    public final Setting<Float> sunFlareIntensity = value("visual", "sunFlareIntensity", 0.25F)
            .addComment("Controls the intensity of the sunglare. Set to 0 to disable the sun flare entirely")
            .addComment("Default: 0.25");
    public final Setting<Boolean> waterOverlayEnabled = value("visual", "waterOverlayEnabled", true)
            .addComment("Sets whether a water splash effect should appear when the player gets wet or is outside in the rain")
            .addComment("Default: true");
    public final Setting<Boolean> hurtOverlayEnabled = value("visual", "hurtOverlayEnabled", true)
            .addComment("Sets whether the edges of the screen flash red whenever the player is hurt")
            .addComment("Default: true");

    public final Setting<Vector2f> digitalEffectPixelRescale = value("visual", "digitalEffectPixelRescale", new Vector2f(0.05F, 0.05F))
            .addComment("Scaling ratio for the zero drug's effect")
            .addComment("Default: {x: 0.05, y: 0.05}");
    private transient float[] digitalEffectPixelRescaleF;

    public final Setting<Boolean> drugsBackgroundMusic = value("audio", "drugsBackgroundMusic", true)
            .addComment("Enables and disables background music to play when certain drugs are active.")
            .addComment("Default: True");

    public final Setting<Float> drugsBackgroundMusicThreshold = value("audio", "drugsBackgroundMusicThreshold", 0.01F)
            .addComment("The threshold value for when background music should start playing. Music only plays if the drug strength is greater than this value")
            .addComment("Default: 0.01")
            .addComment("Minimum: 0.01, Maximum: 1");

    public PSClientConfig(Path path) {
        super(new HeirarchicalJsonConfigAdapter(new GsonBuilder().registerTypeAdapter(DrugType.class, RegistryTypeAdapter.of(DrugType.REGISTRY))), path);
        digitalEffectPixelRescale.onChanged(i -> digitalEffectPixelRescaleF = null);
    }

    public float[] getDigitalEffectPixelResize() {
        if (digitalEffectPixelRescaleF == null) {
            digitalEffectPixelRescaleF = new float[] { digitalEffectPixelRescale.get().x, digitalEffectPixelRescale.get().y };
        }
        return digitalEffectPixelRescaleF;
    }

    public float getBGMThreshold() {
        return MathHelper.clamp(drugsBackgroundMusicThreshold.get(), 0.01F, 1);
    }
}