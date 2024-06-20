package ivorius.psychedelicraft.fluid.physical;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class PhysicalFluid {
    private final Fluid standing;
    private final Fluid flowing;
    private final Block block;

    @Nullable
    private final SimpleFluid type;

    public PhysicalFluid(Fluid standing, Fluid flowing, FluidBlock block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
    }

    public PhysicalFluid(Identifier id, SimpleFluid type) {
        @SuppressWarnings("unused") Object o = Fluids.EMPTY;
        this.type = type;
        standing = Registry.register(Registries.FLUID, id, PlacedFluid.still(this));
        flowing = Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), PlacedFluid.flowing(this));
        block = type.isEmpty() ? Blocks.AIR : Registry.register(Registries.BLOCK, id, PlacedFluidBlock.create(this));
    }

    public Fluid getStandingFluid() {
        return standing;
    }

    public Fluid getFlowingFluid() {
        return flowing;
    }

    public Block getBlock() {
        return block;
    }

    @Nullable
    public Block getCauldron() {
        if (standing == Fluids.WATER) {
            return Blocks.WATER_CAULDRON;
        }
        if (standing == Fluids.LAVA) {
            return Blocks.LAVA_CAULDRON;
        }
        return null;
    }

    @Nullable
    public SimpleFluid getType() {
        return type;
    }

    public FluidState getDefaultState() {
        return getStandingFluid().getDefaultState();
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return standing.isIn(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getStandingFluid().matchesType(fluid);
    }
}
