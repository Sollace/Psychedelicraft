package ivorius.psychedelicraft.client.rendering;

import static ivorius.psychedelicraft.Psychedelicraft.MODID;
import static ivorius.psychedelicraft.Psychedelicraft.filePathTextures;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.blocks.*;
import ivorius.psychedelicraft.client.rendering.blocks.*;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.*;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.item.Item;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        Psychedelicraft.blockWineGrapeLatticeRenderType = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(Psychedelicraft.blockWineGrapeLatticeRenderType, new RenderWineGrapeLattice());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDistillery.class, new TileEntityRendererDistillery());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlask.class, new TileEntityRendererFlask());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMashTub.class, new TileEntityRendererMashTub());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityRendererBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(DryingTableBlockEntity.class, new TileEntityRendererDryingTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPeyote.class, new TileEntityRendererPeyote());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRiftJar.class, new TileEntityRendererRiftJar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBottleRack.class, new TileEntityRendererBottleRack());

        RenderingRegistry.registerEntityRenderingHandler(EntityMolotovCocktail.class, new RenderSnowball(PSItems.molotovCocktail));
        RenderingRegistry.registerEntityRenderingHandler(EntityRealityRift.class, new RenderRealityRift());

        if (PSEntityList.villagerDealerProfessionID >= 0) {
            VillagerRegistry.instance().registerVillagerSkin(PSEntityList.villagerDealerProfessionID, Psychedelicraft.id(Psychedelicraft.filePathTextures + "villagerDealer.png"));
        }

        PSRenderStates.allocate();
        PSRenderStates.outputShaderInfo();
    }
}
