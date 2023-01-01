/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesData;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesDataHandler;
import ivorius.ivtoolkit.network.PacketTileEntityData;
import ivorius.ivtoolkit.network.PacketTileEntityDataHandler;
import ivorius.psychedelicraft.achievements.PSAchievementList;
import ivorius.psychedelicraft.commands.CommandDrug;
import ivorius.psychedelicraft.commands.CommandPsyche;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.crafting.PSCrafting;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.entities.drugs.DrugInfluenceHarmonium;
import ivorius.psychedelicraft.entities.drugs.DrugRegistry;
import ivorius.psychedelicraft.events.*;
import ivorius.psychedelicraft.gui.CreativeTabPsyche;
import ivorius.psychedelicraft.gui.PSGuiHandler;
import ivorius.psychedelicraft.items.PSItems;
import ivorius.psychedelicraft.worldgen.PSWorldGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import org.apache.logging.log4j.Logger;

@Mod(modid = Psychedelicraft.MODID, version = Psychedelicraft.VERSION, name = Psychedelicraft.NAME,
        guiFactory = "ivorius.psychedelicraft.gui.PSConfigGuiFactory")
public class Psychedelicraft
{
    public static final String MODID = "psychedelicraft";
    public static final String NAME = "Psychedelicraft";
    public static final String VERSION = "1.5.2";

    @Instance(value = "psychedelicraft")
    public static Psychedelicraft instance;

    @SidedProxy(clientSide = "ivorius.psychedelicraft.client.ClientProxy", serverSide = "ivorius.psychedelicraft.server.ServerProxy")
    public static PSProxy proxy;

    public static Logger logger;
    public static Configuration config;

    public static PSGuiHandler guiHandler;
    public static PSEventForgeHandler eventForgeHandler;
    public static PSEventFMLHandler eventFMLHandler;
    public static PSCommunicationHandler communicationHandler;

    public static SimpleNetworkWrapper network;

    public static PSCoreHandlerClient coreHandlerClient;
    public static PSCoreHandlerCommon coreHandlerCommon;
    public static PSCoreHandlerServer coreHandlerServer;

    public static CreativeTabPsyche creativeTab;
    public static CreativeTabPsyche drinksTab;
    public static CreativeTabPsyche weaponsTab;

    public static String filePathTextures = "textures/mod/";
    public static String filePathModels = "models/";
    public static String filePathOther = "other/";
    public static String filePathShaders = "shaders/";
    public static String modBase = "psychedelicraft:";

    public static EntityPlayer.EnumStatus sleepStatusDrugs;
    public static DamageSource alcoholPoisoning;
    public static DamageSource respiratoryFailure;
    public static DamageSource stroke;
    public static DamageSource heartFailure;

    public static int blockWineGrapeLatticeRenderType;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        PSConfig.loadConfig(null);
        if (config.hasChanged())
            config.save();

        creativeTab = new CreativeTabPsyche("psychedelicraft");
        drinksTab = new CreativeTabPsyche("psycheDrinks");
        weaponsTab = new CreativeTabPsyche("psycheWeapons");

        guiHandler = new PSGuiHandler();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        eventForgeHandler = new PSEventForgeHandler();
        eventForgeHandler.register();
        eventFMLHandler = new PSEventFMLHandler();
        eventFMLHandler.register();

        communicationHandler = new PSCommunicationHandler(logger, MODID, this);

        coreHandlerCommon = new PSCoreHandlerCommon();
        coreHandlerCommon.register();

        sleepStatusDrugs = EnumHelper.addStatus("onDrugs");
        alcoholPoisoning = new DamageSource("alcoholPoisoning").setDamageBypassesArmor().setDamageIsAbsolute();
        respiratoryFailure = new DamageSource("respiratoryFailure").setDamageBypassesArmor().setDamageIsAbsolute();
        stroke = new DamageSource("stroke").setDamageBypassesArmor().setDamageIsAbsolute();
        heartFailure = new DamageSource("heartFailure").setDamageBypassesArmor().setDamageIsAbsolute();

        PSRegistryHandler.preInit(event, this);

        proxy.preInit();
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(PacketExtendedEntityPropertiesDataHandler.class, PacketExtendedEntityPropertiesData.class, 0, Side.CLIENT);
        network.registerMessage(PacketTileEntityDataHandler.class, PacketTileEntityData.class, 1, Side.CLIENT);

        proxy.registerRenderers();

        creativeTab.tabIcon = PSItems.cannabisLeaf;
        drinksTab.tabIcon = PSItems.itemBarrel;
        weaponsTab.tabIcon = PSItems.molotovCocktail;

        DrugRegistry.registerInfluence(DrugInfluence.class, "default");
        DrugRegistry.registerInfluence(DrugInfluenceHarmonium.class, "harmonium");

        PSRegistryHandler.load(event, this);

        PSCrafting.initialize();
        PSAchievementList.init();
        PSWorldGen.initWorldGen();

        PSConfig.loadConfig(null); // Reload based on new config stuff, like DrugFactories
        if (config.hasChanged())
            config.save();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt)
    {
        evt.registerServerCommand(new CommandDrug());
        evt.registerServerCommand(new CommandPsyche());
    }
}