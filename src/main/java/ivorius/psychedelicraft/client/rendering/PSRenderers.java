package ivorius.psychedelicraft.client.rendering;

import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.rendering.blocks.*;
import ivorius.psychedelicraft.entities.*;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        EntityRendererRegistry.register(PSEntityList.MOLOTOV_COCKTAIL, context -> new FlyingItemEntityRenderer<>(context, 1, true));
        EntityRendererRegistry.register(PSEntityList.REALITY_RIFT, RenderRealityRift::new);

        BlockEntityRendererRegistry.register(PSBlockEntities.DISTILLERY, TileEntityRendererDistillery::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.FLASK, TileEntityRendererFlask::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.MASH_TUB, TileEntityRendererMashTub::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BARREL, TileEntityRendererBarrel::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.DRYING_TABLE, TileEntityRendererDryingTable::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.RIFT_JAR, TileEntityRendererRiftJar::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BOTTLE_RACK, TileEntityRendererBottleRack::new);
    }
}
