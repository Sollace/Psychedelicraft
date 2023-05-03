package ivorius.psychedelicraft.config;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

public class PSConfig {
    public static final int MINUTE = 20 * 60;

    public Balancing balancing = new Balancing();

    public static class Balancing {
        public int randomTicksUntilRiftSpawn = MINUTE * 180;
        public int dryingTableTickDuration = MINUTE * 16;
        public int ironDryingTableTickDuration = MINUTE * 12;
        public int slurryHardeningTime = MINUTE * 30;

        public boolean enableHarmonium = false;
        public boolean enableRiftJars = false;
        public Generation worldGeneration = new Generation();
        public FluidProperties fluidAttributes = new FluidProperties();
        public MessageDistortion messageDistortion = new MessageDistortion();

        public static class MessageDistortion {
            public boolean incoming = true;
            public boolean outgoing = true;
        }

        public static class FluidProperties {
            static final TickInfo DEFAULT = new TickInfo(40, 40, 30, 30);

            public TickInfo alcInfoWheatHop = new TickInfo(30, 60, 100, 30);
            public TickInfo alcInfoKava = DEFAULT;
            public TickInfo alcInfoWheat = DEFAULT;
            public TickInfo alcInfoCorn = DEFAULT;
            public TickInfo alcInfoPotato = DEFAULT;
            public TickInfo alcInfoTomato = DEFAULT;
            public TickInfo alcInfoAgave = new TickInfo(30, 80, 40, 90);
            public TickInfo alcInfoRedGrapes = DEFAULT;
            public TickInfo alcInfoRice = DEFAULT;
            public TickInfo alcInfoJuniper = DEFAULT;
            public TickInfo alcInfoSugarCane = DEFAULT;
            public TickInfo alcInfoHoney = DEFAULT;
            public TickInfo alcInfoApple = DEFAULT;
            public TickInfo alcInfoPineapple = DEFAULT;
            public TickInfo alcInfoBanana = DEFAULT;
            public TickInfo alcInfoMilk = DEFAULT;
            public TickInfo alcInfoFlowerExtract = new TickInfo(40, 1, 30, 30);

            public static class TickInfo {
                public int ticksPerFermentation;
                public int ticksPerDistillation;
                public int ticksPerMaturation;
                public int ticksUntilAcetification;

                public TickInfo(int fermentation, int distillation, int maturation, int acetification) {
                    ticksPerFermentation = fermentation * MINUTE;
                    ticksPerDistillation = distillation * MINUTE;
                    ticksPerMaturation = maturation * MINUTE;
                    ticksUntilAcetification = acetification * MINUTE;
                }
            }
        }

        public static class Generation {
            public FeatureConfig juniper = new FeatureConfig();
            public FeatureConfig cannabis = new FeatureConfig();
            public FeatureConfig hop = new FeatureConfig();
            public FeatureConfig tobacco = new FeatureConfig();
            public FeatureConfig morningGlories = new FeatureConfig();
            public FeatureConfig belladonna = new FeatureConfig();
            public FeatureConfig jimsonweed = new FeatureConfig();
            public FeatureConfig tomato = new FeatureConfig();
            public FeatureConfig coffea = new FeatureConfig();
            public FeatureConfig coca = new FeatureConfig();
            public FeatureConfig peyote = new FeatureConfig();

            public boolean farmerDrugDeals = true;
            public boolean dungeonChests = true;
            public boolean villageChests = true;

            public static class FeatureConfig {
                public boolean enabled = true;
                public InclusionFilter spawnableBiomes = new InclusionFilter();

                public void ifEnabled(Consumer<InclusionFilter> register) {
                    if (enabled) {
                        register.accept(spawnableBiomes);
                    }
                }
            }

            public static class InclusionFilter {
                public String[] included;
                public String[] excluded;

                public Predicate<BiomeSelectionContext> createPredicate(Predicate<BiomeSelectionContext> inherentPredicate) {
                    return BiomeSelector.compile(included, excluded, inherentPredicate);
                }
            }
        }
    }
}
