package ivorius.psychedelicraft.client;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.joml.Vector2f;

import com.google.gson.GsonBuilder;
import com.minelittlepony.common.util.registry.RegistryTypeAdapter;
import com.minelittlepony.common.util.settings.Config;
import com.minelittlepony.common.util.settings.HeirarchicalJsonConfigAdapter;
import com.minelittlepony.common.util.settings.Setting;
import ivorius.psychedelicraft.entity.drug.DrugType;

public class PSClientConfig extends Config {
    public final Setting<Float> dofFocalPointNear = value("visual", "dofFocalPointNear", 0.2F);
    public final Setting<Float> dofFocalBlurNear = value("visual", "dofFocalBlurNear", 0F);
    public final Setting<Float> dofFocalPointFar = value("visual", "dofFocalPointFar", 128F);
    public final Setting<Float> dofFocalBlurFar = value("visual", "dofFocalBlurFar", 0F);

    public final Setting<Boolean> shader2DEnabled = value("visual", "shader2DEnabled", true);
    public final Setting<Boolean> shader3DEnabled = value("visual", "shader3DEnabled", true);

    public final Setting<Boolean> doHeatDistortion = value("visual", "doHeatDistortion", true);
    public final Setting<Boolean> doWaterDistortion = value("visual", "doWaterDistortion", true);
    public final Setting<Boolean> doMotionBlur = value("visual", "doMotionBlur", true);

    public final Setting<Float> sunFlareIntensity = value("visual", "sunFlareIntensity", 0.25F);
    //public final Setting<Integer> shadowPixelsPerChunk = value("visual", "shadowPixelsPerChunk", 256);

    public final Setting<Boolean> waterOverlayEnabled = value("visual", "waterOverlayEnabled", true);
    public final Setting<Boolean> hurtOverlayEnabled = value("visual", "hurtOverlayEnabled", true);

    public final Setting<Vector2f> digitalEffectPixelRescale = value("visual", "digitalEffectPixelRescale", new Vector2f(0.05F, 0.05F));
    private transient float[] digitalEffectPixelRescaleF;

    public final Setting<Set<DrugType<?>>> drugsWithBackgroundMusic = value("audio", "drugsWithBackgroundMusic", DrugType.REGISTRY.stream().collect(Collectors.toUnmodifiableSet()));

    // (Sollace) made transient because this config was disabled before
    //public transient boolean doShadows = false;

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

    public boolean hasBackgroundMusic(DrugType<?> drugType) {
        return drugsWithBackgroundMusic.get().contains(drugType);
    }

    public boolean setHasBackgroundMusic(DrugType<?> drugType, boolean value) {
        var values = new HashSet<>(drugsWithBackgroundMusic.get());
        if (value) {
            values.add(drugType);
        } else {
            values.remove(drugType);
        }
        drugsWithBackgroundMusic.set(values);

        return value;
    }
}