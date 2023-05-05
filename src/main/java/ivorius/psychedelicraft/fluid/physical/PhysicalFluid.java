package ivorius.psychedelicraft.fluid.physical;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class PhysicalFluid {
    final Fluid standing;
    final Fluid flowing;
    final Block block;

    @Nullable
    final SimpleFluid type;

    public PhysicalFluid(Fluid standing, Fluid flowing, FluidBlock block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
    }

    public PhysicalFluid(Identifier id, SimpleFluid type) {
        this.type = type;
        standing = Registry.register(Registries.FLUID, id, PlacedFluid.still(this));
        flowing = Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), PlacedFluid.flowing(this));
        block = type.isEmpty() ? Blocks.AIR : Registry.register(Registries.BLOCK, id, new PlacedFluidBlock((FlowableFluid)flowing) {
            @Override
            protected PhysicalFluid getPysicalFluid() {
                return PhysicalFluid.this;
            }
        });
    }

    public Fluid getFluid() {
        return standing;
    }

    public Fluid getFlowingFluid() {
        return flowing;
    }

    public FluidState getDefaultState() {
        return getFluid().getDefaultState();
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return standing.isIn(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getFluid().matchesType(fluid);
    }
}
