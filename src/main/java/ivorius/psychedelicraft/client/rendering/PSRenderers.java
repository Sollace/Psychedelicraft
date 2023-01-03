package ivorius.psychedelicraft.client.rendering;

import static ivorius.psychedelicraft.Psychedelicraft.MODID;
import static ivorius.psychedelicraft.Psychedelicraft.filePathTextures;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.*;
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
        ClientRegistry.bindTileEntitySpecialRenderer(DistilleryBlockEntity.class, new TileEntityRendererDistillery());
        ClientRegistry.bindTileEntitySpecialRenderer(FlaskBlockEntity.class, new TileEntityRendererFlask());
        ClientRegistry.bindTileEntitySpecialRenderer(MashTubBlockEntity.class, new TileEntityRendererMashTub());
        ClientRegistry.bindTileEntitySpecialRenderer(BarrelBlockEntity.class, new TileEntityRendererBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(DryingTableBlockEntity.class, new TileEntityRendererDryingTable());
        ClientRegistry.bindTileEntitySpecialRenderer(PeyoteBlockEntity.class, new TileEntityRendererPeyote());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRiftJar.class, new TileEntityRendererRiftJar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBottleRack.class, new TileEntityRendererBottleRack());

        RenderingRegistry.registerEntityRenderingHandler(EntityMolotovCocktail.class, new RenderSnowball(PSItems.molotovCocktail));
        RenderingRegistry.registerEntityRenderingHandler(EntityRealityRift.class, new RenderRealityRift());

        PSRenderStates.allocate();
        PSRenderStates.outputShaderInfo();
    }
}
