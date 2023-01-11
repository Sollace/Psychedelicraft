package ivorius.psychedelicraft.client.rendering;

import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.client.rendering.blocks.*;
import ivorius.psychedelicraft.entities.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        EntityRendererRegistry.register(PSEntities.MOLOTOV_COCKTAIL, context -> new FlyingItemEntityRenderer<>(context, 1, true));
        EntityRendererRegistry.register(PSEntities.REALITY_RIFT, RealityRiftEntityRenderer::new);

        BlockEntityRendererRegistry.register(PSBlockEntities.DISTILLERY, DistilleryBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.FLASK, FlaskBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.MASH_TUB, MashTubBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BARREL, BarrelBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.DRYING_TABLE, DryingTableBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.RIFT_JAR, RiftJarBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BOTTLE_RACK, BottleRackBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), PSBlocks.DISTILLERY);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), PSBlocks.JUNIPER_LEAVES, PSBlocks.FRUITING_JUNIPER_LEAVES, PSBlocks.LATTICE, PSBlocks.WINE_GRAPE_LATTICE);

    }
}
