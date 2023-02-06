package ivorius.psychedelicraft.client.render;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.particle.ExhaledSmokeParticle;
import ivorius.psychedelicraft.client.render.blocks.*;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
import ivorius.psychedelicraft.entity.*;
import ivorius.psychedelicraft.fluid.FluidContainer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.particle.PSParticles;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(PSParticles.EXHALED_SMOKE, createFactory(ExhaledSmokeParticle::new));

        EntityRendererRegistry.register(PSEntities.MOLOTOV_COCKTAIL, context -> new FlyingItemEntityRenderer<>(context, 1, true));
        EntityRendererRegistry.register(PSEntities.REALITY_RIFT, RealityRiftEntityRenderer::new);

        BlockEntityRendererRegistry.register(PSBlockEntities.DISTILLERY, FlaskBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.FLASK, FlaskBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.MASH_TUB, MashTubBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BARREL, BarrelBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.DRYING_TABLE, DryingTableBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.RIFT_JAR, RiftJarBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.BOTTLE_RACK, BottleRackBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PSBlockEntities.PEYOTE, PeyoteBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), PSBlocks.DISTILLERY, PSBlocks.FLASK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                PSBlocks.JUNIPER_SAPLING, PSBlocks.JUNIPER_LEAVES, PSBlocks.FRUITING_JUNIPER_LEAVES, PSBlocks.LATTICE, PSBlocks.WINE_GRAPE_LATTICE,
                PSBlocks.CANNABIS, PSBlocks.HOP, PSBlocks.TOBACCO, PSBlocks.COCA, PSBlocks.COFFEA,
                PSBlocks.MASH_TUB);

        BuiltinItemRendererRegistry.INSTANCE.register(PSItems.RIFT_JAR, RiftJarBlockEntityRenderer::renderStack);

        SimpleFluid.all().forEach(fluid -> {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid.getPhysical().getFluid(), fluid.getPhysical().getFlowingFluid(), new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL, SimpleFluidRenderHandler.WATER_FLOWING, SimpleFluidRenderHandler.WATER_OVERLAY, 0) {
                @Override
                public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                    return fluid.getColor(fluid.getStack(state, FluidContainer.UNLIMITED));
                }
            });
        });

        PSShaders.bootstrap();
    }

    private static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}
