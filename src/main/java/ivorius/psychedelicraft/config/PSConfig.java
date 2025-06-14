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

    public final Setting<Integer> randomTicksUntilRiftSpawn = value("balancing", "randomTicksUntilRiftSpawn", MINUTE * 180)
            .addComment("Controls how frequently zero rifts spawn.")
            .addComment("Set to 0 to disable rift spawning entirely")
            .addComment("Default: " + (MINUTE * 180));
    public final Setting<Integer> dryingTableTickDuration = value("balancing", "dryingTableTickDuration", MINUTE * 16)
            .addComment("Sets the number of ticks the wooden drying table takes on average to cook items")
            .addComment("Default: " + (MINUTE * 16));
    public final Setting<Integer> ironDryingTableTickDuration = value("balancing", "ironDryingTableTickDuration", MINUTE * 12)
            .addComment("Sets the number of ticks the stone drying table takes on average to cook items")
            .addComment("Default: " + (MINUTE * 12));
    public final Setting<Integer> slurryHardeningTime = value("balancing", "slurryHardeningTime", MINUTE * 30)
            .addComment("Sets the number of ticks it takes for slurry to congeal into dirt")
            .addComment("Default: " + (MINUTE * 30));
    public final Setting<Boolean> enableHarmonium = value("balancing", "enableHarmonium", true)
            .addComment("Sets whether harmonium is obtainable")
            .addComment("Default: true");
    public final Setting<Boolean> enableRiftJars = value("balancing", "enableRiftJars", true)
            .addComment("Sets whether rift jars are obtainable. Rift jars are only useful if rift spawning is enabled as well.")
            .addComment("Default: true");
    public final Setting<Boolean> disableMolotovs = value("balancing", "disableMolotovs", false)
            .addComment("Sets whether molotov cocktails are (not) obtainable.")
            .addComment("Default: false");

    public final Setting<Generation> worldGeneration = value("balancing", "worldGeneration", new Generation(
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT, FeatureCustomConfig.DEFAULT,
            FeatureCustomConfig.DEFAULT,
            true, true, true
    )).addComment("Settings affecting world generation and in-game mechanics");
    public final Setting<TickRates> fluidAttributes = value("balancing", "fluidAttributes", new TickRates(TickRate.getDefaults()))
            .addComment("Sets the rate at which each fluid is processed.")
            .addComment("Delete the entry for a fluid and restart the game to have the default value for that fluid populated.");
    public final Setting<MessageDistortion> messageDistortion = value("balancing", "messageDistortion", MessageDistortion.BOTH)
            .addComment("Sets whether drug effects are able to mess with chat messages.")
            .addComment("Default: BOTH")
            .addComment("OUTGOING - Messages others see from you are affected")
            .addComment("INCOMING - Messages you see from others are affected")
            .addComment("BOTH - All messages are affected")
            .addComment("NONE - No messages are affected");

    public PSConfig(Path path) {
        super(new HeirarchicalJsonConfigAdapter(new GsonBuilder()
                .registerTypeAdapter(FeatureCustomConfig.InclusionFilter.class, new CodecTypeAdapter<>(FeatureCustomConfig.InclusionFilter.CODEC))
                .registerTypeAdapter(TickRates.class, new CodecTypeAdapter<>(TickRates.CODEC))
        ), path);
        enableHarmonium.onChanged(PSConfig::forceInventoryGroupsRefresh);
        enableRiftJars.onChanged(PSConfig::forceInventoryGroupsRefresh);
        disableMolotovs.onChanged(PSConfig::forceInventoryGroupsRefresh);
    }

    private static void forceInventoryGroupsRefresh(boolean v) {
        var context = ItemGroups.displayContext;
        if (context != null) {
            ItemGroups.displayContext = null;
            ItemGroups.updateDisplayContext(context.enabledFeatures(), context.hasPermissions(), context.lookup());
        }
    }
}
