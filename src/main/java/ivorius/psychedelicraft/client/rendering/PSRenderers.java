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
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        ClientRegistry.bindTileEntitySpecialRenderer(DistilleryBlockEntity.class, new TileEntityRendererDistillery());
        ClientRegistry.bindTileEntitySpecialRenderer(FlaskBlockEntity.class, new TileEntityRendererFlask());
        ClientRegistry.bindTileEntitySpecialRenderer(MashTubBlockEntity.class, new TileEntityRendererMashTub());
        ClientRegistry.bindTileEntitySpecialRenderer(BarrelBlockEntity.class, new TileEntityRendererBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(DryingTableBlockEntity.class, new TileEntityRendererDryingTable());
        ClientRegistry.bindTileEntitySpecialRenderer(PeyoteBlockEntity.class, new TileEntityRendererPeyote());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRiftJar.class, new TileEntityRendererRiftJar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBottleRack.class, new TileEntityRendererBottleRack());

        EntityRendererRegistry.register(PSEntityList.MOLOTOV_COCKTAIL, context -> new FlyingItemEntityRenderer<>(context, 1, true));
        EntityRendererRegistry.register(PSEntityList.REALITY_RIFT, RenderRealityRift::new);
        //PSRenderStates.allocate();
        //PSRenderStates.outputShaderInfo();
    }
}
