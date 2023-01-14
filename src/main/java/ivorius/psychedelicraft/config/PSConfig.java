package ivorius.psychedelicraft.config;

public class PSConfig {
    public static int MINUTE = 20 * 60;

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
        //public MessageDistortion messageDistortion = new MessageDistortion();

        // TODO: (Sollace) reimplement message distortion
        /*
        public static class MessageDistortion {
            public boolean incoming = true;
            public boolean outgoing = true;
        }*/

        public static class FluidProperties {
            static TickInfo DEFAULT = new TickInfo(40, 40, 30, 30);

            public TickInfo alcInfoWheatHop = new TickInfo(30, 60, 100, 30);
            public TickInfo alcInfoWheat = DEFAULT;
            public TickInfo alcInfoCorn = DEFAULT;
            public TickInfo alcInfoPotato = DEFAULT;
            public TickInfo alcInfoRedGrapes = DEFAULT;
            public TickInfo alcInfoRice = DEFAULT;
            public TickInfo alcInfoJuniper = DEFAULT;
            public TickInfo alcInfoSugarCane = DEFAULT;
            public TickInfo alcInfoHoney = DEFAULT;
            public TickInfo alcInfoApple = DEFAULT;
            public TickInfo alcInfoPineapple = DEFAULT;
            public TickInfo alcInfoBanana = DEFAULT;
            public TickInfo alcInfoMilk = DEFAULT;

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

        // TODO: (Sollace) reimplement world gen features
        public static class Generation {
           // public boolean genJuniper = true;
           // public boolean genCannabis = true;
           // public boolean genHop = true;
           // public boolean genTobacco = true;
           // public boolean genCoffea = true;
           // public boolean genCoca = true;
           // public boolean genPeyote = true;
            public boolean farmerDrugDeals = true;
        }
    }
}
