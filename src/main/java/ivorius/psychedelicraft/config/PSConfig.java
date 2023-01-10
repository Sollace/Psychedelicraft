package ivorius.psychedelicraft.config;

public class PSConfig {
    public static final int MINUTE = 20 * 60;

    public final Balancing balancing = new Balancing();

    public static class Balancing {
        public final int randomTicksUntilRiftSpawn = MINUTE * 180;
        public final int dryingTableTickDuration = MINUTE * 16;
        public final int ironDryingTableTickDuration = MINUTE * 12;
        public final int slurryHardeningTime = MINUTE * 30;

        public final boolean enableHarmonium = false;
        public final boolean enableRiftJars = false;
        public final Generation worldGeneration = new Generation();
        public final FluidProperties fluidAttributes = new FluidProperties();
        //public final MessageDistortion messageDistortion = new MessageDistortion();

        // TODO: reimplement message distortion
        /*
        public static class MessageDistortion {
            public final boolean incoming = true;
            public final boolean outgoing = true;
        }*/

        public static class FluidProperties {
            static final TickInfo DEFAULT = new TickInfo(40, 40, 30, 30);

            public final TickInfo alcInfoWheatHop = new TickInfo(30, 60, 100, 30);
            public final TickInfo alcInfoWheat = DEFAULT;
            public final TickInfo alcInfoCorn = DEFAULT;
            public final TickInfo alcInfoPotato = DEFAULT;
            public final TickInfo alcInfoRedGrapes = DEFAULT;
            public final TickInfo alcInfoRice = DEFAULT;
            public final TickInfo alcInfoJuniper = DEFAULT;
            public final TickInfo alcInfoSugarCane = DEFAULT;
            public final TickInfo alcInfoHoney = DEFAULT;
            public final TickInfo alcInfoApple = DEFAULT;
            public final TickInfo alcInfoPineapple = DEFAULT;
            public final TickInfo alcInfoBanana = DEFAULT;
            public final TickInfo alcInfoMilk = DEFAULT;

            public static class TickInfo {
                public final int ticksPerFermentation;
                public final int ticksPerDistillation;
                public final int ticksPerMaturation;
                public final int ticksUntilAcetification;

                public TickInfo(int fermentation, int distillation, int maturation, int acetification) {
                    ticksPerFermentation = fermentation * MINUTE;
                    ticksPerDistillation = distillation * MINUTE;
                    ticksPerMaturation = maturation * MINUTE;
                    ticksUntilAcetification = acetification * MINUTE;
                }
            }
        }

        // TODO: reimplement world gen features
        public static class Generation {
           // public final boolean genJuniper = true;
           // public final boolean genCannabis = true;
           // public final boolean genHop = true;
           // public final boolean genTobacco = true;
           // public final boolean genCoffea = true;
           // public final boolean genCoca = true;
           // public final boolean genPeyote = true;
            public final boolean farmerDrugDeals = true;
        }
    }
}
