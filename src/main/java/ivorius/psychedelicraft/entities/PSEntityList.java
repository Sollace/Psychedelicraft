/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import ivorius.psychedelicraft.blocks.*;
import ivorius.psychedelicraft.config.PSConfig;

/**
 * Created by lukas on 25.04.14.
 */
public class PSEntityList
{
    public static final int molotovCocktailID = 0;
    public static final int realityRiftID = 1;

    public static int villagerDealerProfessionID;

    public static void bootstrap() {
        int entityMolotovCocktailID = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityMolotovCocktail.class, "molotovCocktail", entityMolotovCocktailID);
        EntityRegistry.registerModEntity(EntityMolotovCocktail.class, "molotovCocktail", entityMolotovCocktailID, mod, 64, 10, true);
        EntityRegistry.registerModEntity(EntityRealityRift.class, "realityRift", realityRiftID, mod, 80, 3, false);
        EntityRegistry.registerModEntity(EntityMolotovCocktail.class, "molotovCocktail", molotovCocktailID, mod, 64, 10, true);

        if (villagerDealerProfessionID >= 0) {
            VillagerRegistry.instance().registerVillagerId(villagerDealerProfessionID);
            VillagerRegistry.instance().registerVillageTradeHandler(villagerDealerProfessionID, new VillagerTradeHandlerDrugDealer());
        }

        if (PSConfig.farmerDrugDeals) {
            VillagerRegistry.instance().registerVillageTradeHandler(0, new VillagerTradeHandlerFarmer());
        }

        GameRegistry.registerTileEntityWithAlternatives(TileEntityDryingTable.class, "ygcDryingTable", "dryingTable");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityMashTub.class, "ygcMashTub");
        GameRegistry.registerTileEntity(TileEntityDistillery.class, "psDistillery");
        GameRegistry.registerTileEntity(TileEntityFlask.class, "psFlask");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityBarrel.class, "ygcBarrel", "barrel");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityDryingTable.class, "ygcIronDryingTable", "ironDryingTable");
    }
}
