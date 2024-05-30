package ivorius.psychedelicraft.config;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

public class PSConfig {
    public static final int MINUTE = 20 * 60;

    public Balancing balancing = new Balancing();

    public static final class Balancing {
        public int randomTicksUntilRiftSpawn = MINUTE * 180;
        public int dryingTableTickDuration = MINUTE * 16;
        public int ironDryingTableTickDuration = MINUTE * 12;
        public int slurryHardeningTime = MINUTE * 30;
        public boolean enableHarmonium;
        public boolean enableRiftJars;
        public boolean disableMolotovs;
        public Generation worldGeneration = new Generation(
                Generation.FeatureConfig.DEFAULT, Generation.FeatureConfig.DEFAULT,
                Generation.FeatureConfig.DEFAULT, Generation.FeatureConfig.DEFAULT,
                Generation.FeatureConfig.DEFAULT, Generation.FeatureConfig.DEFAULT,
                Generation.FeatureConfig.DEFAULT, Generation.FeatureConfig.DEFAULT,
                Generation.FeatureConfig.DEFAULT, Generation.FeatureConfig.DEFAULT,
                Generation.FeatureConfig.DEFAULT,
                true, true, true
        );
        public FluidProperties fluidAttributes = new Balancing.FluidProperties(
                new FluidProperties.TickInfo(30 * MINUTE, 60 * MINUTE, 100 * MINUTE, 30 * MINUTE),
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                new FluidProperties.TickInfo(30 * MINUTE, 80 * MINUTE, 40 * MINUTE, 90 * MINUTE),
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                FluidProperties.TickInfo.DEFAULT,
                new FluidProperties.TickInfo(40 * MINUTE, 1 * MINUTE, 30 * MINUTE, 30 * MINUTE)
        );
        public MessageDistortion messageDistortion = new MessageDistortion();

        public static final class MessageDistortion {
            public boolean incoming = true;
            public boolean outgoing = true;
        }
        public record FluidProperties (
                TickInfo alcInfoWheatHop,
                TickInfo alcInfoKava,
                TickInfo alcInfoWheat,
                TickInfo alcInfoCorn,
                TickInfo alcInfoPotato,
                TickInfo alcInfoTomato,
                TickInfo alcInfoAgave,
                TickInfo alcInfoRedGrapes,
                TickInfo alcInfoRice,
                TickInfo alcInfoJuniper,
                TickInfo alcInfoSugarCane,
                TickInfo alcInfoHoney,
                TickInfo alcInfoApple,
                TickInfo alcInfoPineapple,
                TickInfo alcInfoBanana,
                TickInfo alcInfoMilk,
                TickInfo alcInfoFlowerExtract) {
            public record TickInfo (int ticksPerFermentation, int ticksPerDistillation, int ticksPerMaturation, int ticksUntilAcetification) {
                static final TickInfo DEFAULT = new TickInfo(40 * MINUTE, 40 * MINUTE, 30 * MINUTE, 30 * MINUTE);
            }
        }

        public record Generation (
                FeatureConfig juniper,
                FeatureConfig cannabis,
                FeatureConfig hop,
                FeatureConfig tobacco,
                FeatureConfig morningGlories,
                FeatureConfig belladonna,
                FeatureConfig jimsonweed,
                FeatureConfig tomato,
                FeatureConfig coffea,
                FeatureConfig coca,
                FeatureConfig peyote,

                boolean farmerDrugDeals,
                boolean dungeonChests,
                boolean villageChests) {
            public record FeatureConfig(boolean enabled, InclusionFilter spawnableBiomes) {
                static final FeatureConfig DEFAULT = new FeatureConfig(true, new InclusionFilter(new String[0], new String[0]));
                public void ifEnabled(Consumer<InclusionFilter> register) {
                    if (enabled) {
                        register.accept(spawnableBiomes);
                    }
                }

                public record InclusionFilter (String[] included, String[] excluded) {
                    public Predicate<BiomeSelectionContext> createPredicate(Predicate<BiomeSelectionContext> inherentPredicate) {
                        return BiomeSelector.compile(included, excluded, inherentPredicate);
                    }
                }
            }
        }
    }
}
