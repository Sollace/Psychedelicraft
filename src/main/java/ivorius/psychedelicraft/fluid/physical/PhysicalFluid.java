package ivorius.psychedelicraft.fluid.physical;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class PhysicalFluid {
    private final Supplier<Fluid> standing;
    private final Supplier<Fluid> flowing;
    private final Supplier<Block> block;

    @Nullable
    private final SimpleFluid type;

    public PhysicalFluid(Supplier<Fluid> standing, Supplier<Fluid> flowing, Supplier<Block> block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
    }

    public PhysicalFluid(Identifier id, SimpleFluid type) {
        @SuppressWarnings("unused") Object o = Fluids.EMPTY;
        this.type = type;
        standing = Suppliers.ofInstance(Registry.register(Registries.FLUID, id, PlacedFluid.still(this)));
        flowing = Suppliers.ofInstance(Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), PlacedFluid.flowing(this)));
        block = Suppliers.ofInstance(type.isEmpty() ? Blocks.AIR : PlacedFluidBlock.create(id, this));
    }

    public Fluid getStandingFluid() {
        return standing.get();
    }

    public Fluid getFlowingFluid() {
        return flowing.get();
    }

    public Block getBlock() {
        return block.get();
    }

    @Nullable
    public Block getCauldron() {
        if (standing == Fluids.WATER) {
            return Blocks.WATER_CAULDRON;
        }
        if (standing == Fluids.LAVA && Blocks.LAVA_CAULDRON.getDefaultState().contains(LeveledCauldronBlock.LEVEL)) {
            return Blocks.LAVA_CAULDRON;
        }
        return PSBlocks.CAULDRON;
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
        return getStandingFluid().isIn(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getStandingFluid().matchesType(fluid);
    }
}
