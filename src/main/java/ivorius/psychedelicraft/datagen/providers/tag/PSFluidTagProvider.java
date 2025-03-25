package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.FluidTags;

public class PSFluidTagProvider extends FabricTagProvider.FluidTagProvider {
    public PSFluidTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(FluidTags.WATER).add(SimpleFluid.REGISTRY.stream().filter(i -> !i.isEmpty() && i.isCustomFluid()).flatMap(i -> Stream.of(
                i.getPhysical().getStandingFluid(),
                i.getPhysical().getFlowingFluid()
        )).toArray(Fluid[]::new));
    }
}
