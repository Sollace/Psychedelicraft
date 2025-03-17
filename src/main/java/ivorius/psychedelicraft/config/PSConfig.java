package ivorius.psychedelicraft.config;

import java.nio.file.Path;

import com.google.gson.GsonBuilder;
import com.minelittlepony.common.util.settings.Config;
import com.minelittlepony.common.util.settings.HeirarchicalJsonConfigAdapter;
import com.minelittlepony.common.util.settings.Setting;

import ivorius.psychedelicraft.fluid.alcohol.TickRate;
import ivorius.psychedelicraft.util.CodecTypeAdapter;
import net.minecraft.item.ItemGroups;

public class PSConfig extends Config {
    public static final int MINUTE = 20 * 60;

    public final Setting<Integer> randomTicksUntilRiftSpawn = value("balancing", "randomTicksUntilRiftSpawn", MINUTE * 180);
    public final Setting<Integer> dryingTableTickDuration = value("balancing", "dryingTableTickDuration", MINUTE * 16);
    public final Setting<Integer> ironDryingTableTickDuration = value("balancing", "ironDryingTableTickDuration", MINUTE * 12);
    public final Setting<Integer> slurryHardeningTime = value("balancing", "slurryHardeningTime", MINUTE * 30);
    public final Setting<Boolean> enableHarmonium = value("balancing", "enableHarmonium", true);
    public final Setting<Boolean> enableRiftJars = value("balancing", "enableRiftJars", true);
    public final Setting<Boolean> disableMolotovs = value("balancing", "disableMolotovs", false);

    public final Setting<Generation> worldGeneration = value("balancing", "worldGeneration", new Generation(
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT,
            true, true, true
    ));
    public final Setting<TickRates> fluidAttributes = value("balancing", "fluidAttributes", new TickRates(TickRate.getDefaults()));
    public final Setting<MessageDistortion> messageDistortion = value("balancing", "messageDistortion", MessageDistortion.BOTH);

    public PSConfig(Path path) {
        super(new HeirarchicalJsonConfigAdapter(new GsonBuilder()
                .registerTypeAdapter(FeatureCustomConfig.InclusionFilter.class, new CodecTypeAdapter<>(FeatureCustomConfig.InclusionFilter.CODEC))
                .registerTypeAdapter(TickRates.class, new CodecTypeAdapter<>(TickRates.CODEC))
        ), path);
        enableHarmonium.onChanged(v -> ItemGroups.displayContext = null);
        enableRiftJars.onChanged(v -> ItemGroups.displayContext = null);
        disableMolotovs.onChanged(v -> ItemGroups.displayContext = null);
    }
}
