/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.config;

import ivorius.psychedelicraft.PSProxy;
import ivorius.psychedelicraft.fluids.AlcoholicFluid;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 31.07.14.
 */
public class PSConfig
{
    public static final String CATEGORY_BALANCING = "balancing";
    public static final String CATEGORY_VISUAL = "visual";
    public static final String CATEGORY_AUDIO = "audio";

    private static final int MINUTE = 20 * 60;

    public static int randomTicksUntilRiftSpawn;
    public static boolean enableHarmonium;
    public static boolean enableRiftJars;

    public static boolean genJuniper;
    public static boolean genCannabis;
    public static boolean genHop;
    public static boolean genTobacco;
    public static boolean genCoffea;
    public static boolean genCoca;
    public static boolean genPeyote;

    public static boolean dungeonChests;
    public static boolean villageChests;

    public static boolean farmerDrugDeals;

    public static int dryingTableTickDuration;
    public static int ironDryingTableTickDuration;

    public static final Map<String, Boolean> drugBGM = new HashMap<>();

    public static final AlcoholicFluid.TickInfo alcInfoWheatHop = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoWheat = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoCorn = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoPotato = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoRedGrapes = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoRice = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoJuniper = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoSugarCane = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoHoney = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoApple = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoPineapple = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoBanana = new AlcoholicFluid.TickInfo();
    public static final AlcoholicFluid.TickInfo alcInfoMilk = new AlcoholicFluid.TickInfo();

    public static int slurryHardeningTime;

    public static boolean distortIncomingMessages;
    public static boolean distortOutgoingMessages;

    public static boolean villagerDealer;

    public static void loadConfig(String configID, Configuration config)
    {
        if (configID == null || configID.equals(Configuration.CATEGORY_GENERAL))
        {
            villagerDealer = config.get("General", "villagerDealerProfessionID", false, "Internal ID for the drug dealer villager. Enter a negative number to disable.");
        }

        if (configID == null || configID.equals(CATEGORY_BALANCING))
        {
            randomTicksUntilRiftSpawn = config.get(CATEGORY_BALANCING, "randomTicksUntilRiftSpawn", MINUTE * 180, "Approximate number of ticks until a rift spawns. Enter a negative number to disable.");

            enableHarmonium = config.get(CATEGORY_BALANCING, "enableHarmonium", false);
            enableRiftJars = config.get(CATEGORY_BALANCING, "enableRiftJars", false);

            genJuniper = config.get(CATEGORY_BALANCING, "generateJuniper", true);
            genCannabis = config.get(CATEGORY_BALANCING, "generateCannabis", true);
            genHop = config.get(CATEGORY_BALANCING, "genHop", true);
            genTobacco = config.get(CATEGORY_BALANCING, "generateTobacco", true);
            genCoffea = config.get(CATEGORY_BALANCING, "generateCoffea", true);
            genCoca = config.get(CATEGORY_BALANCING, "generateCoca", true);
            genPeyote = config.get(CATEGORY_BALANCING, "generatePeyote", true);

            dungeonChests = config.get(CATEGORY_BALANCING, "dungeonChests", true);
            villageChests = config.get(CATEGORY_BALANCING, "villageChests", true);

            farmerDrugDeals = config.get(CATEGORY_BALANCING, "farmerDrugDeals", true);

            dryingTableTickDuration = config.get(CATEGORY_BALANCING, "dryingTableTickDuration", MINUTE * 16, "Time until plants in the drying table finish the drying process.");
            ironDryingTableTickDuration = config.get(CATEGORY_BALANCING, "ironDryingTableTickDuration", MINUTE * 12, "Time until plants in the iron drying table finish the drying process.");

            readTickInfo(alcInfoWheatHop, "wheatHop", MINUTE * 30, MINUTE * 60, MINUTE * 100, MINUTE * 30, config);
            readTickInfo(alcInfoWheat, "wheat", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoCorn, "corn", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoPotato, "potato", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoRedGrapes, "redGrapes", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoRice, "rice", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoJuniper, "juniper", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoSugarCane, "sugarCane", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoHoney, "honey", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoApple, "apple", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoPineapple, "pineapple", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoBanana, "banana", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);
            readTickInfo(alcInfoMilk, "milk", MINUTE * 40, MINUTE * 40, MINUTE * 30, MINUTE * 30, config);

            slurryHardeningTime = config.get(CATEGORY_BALANCING, "slurryHardeningTime", MINUTE * 30, "The amount of ticks slurry needs to sit in a vat to harden to dirt.");

            distortOutgoingMessages = config.get(CATEGORY_BALANCING, "distortOutgoingMessages", true, "Whether the mod should distort chat messages when drugs have been consumed ('slurred speech').");
        }

        PSProxy.getInstance().loadConfig(config, configID);
    }

    public static void readTickInfo(AlcoholicFluid.TickInfo tickInfo, String fluidName, int defaultFermentation, int defaultDistillation, int defaultMaturation, int defaultAcetification, Configuration config)
    {
        tickInfo.ticksPerFermentation = config.get(CATEGORY_BALANCING, fluidName + "_ticksPerFermentation", defaultFermentation, String.format("Time until %s wort ferments to the next step.", fluidName));
        tickInfo.ticksPerDistillation = config.get(CATEGORY_BALANCING, fluidName + "_ticksPerDistillation", defaultDistillation, String.format("Time until %s distills to the next step.", fluidName));
        tickInfo.ticksPerMaturation = config.get(CATEGORY_BALANCING, fluidName + "_ticksPerMaturation", defaultMaturation, String.format("Time until %s matures to the next step.", fluidName));
        tickInfo.ticksUntilAcetification = config.get(CATEGORY_BALANCING, fluidName + "_ticksUntilAcetification", defaultAcetification, String.format("Time until %s acetifies to form vinegar. Enter -1 to disable.", fluidName));
    }

    public static void readHasBGM(String drugName, Configuration config)
    {
        drugBGM.put(drugName, config.get(CATEGORY_AUDIO, "bgm_" + drugName, false, "Indicates if the drug is supposed to have background music when active (refer to the wiki for instructions)."));
    }

    public static boolean hasBGM(String drugName)
    {
        Boolean bool = drugBGM.get(drugName);
        return bool != null && bool;
    }
}
