package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.crafting.Pourable;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface FluidContainerItem extends Pourable {
    int getMaxCapacity();

    default int getMaxCapacity(ItemStack stack) {
        return stack.getItem() instanceof FluidContainerItem f ? f.getMaxCapacity() : 0;
    }

    default Fluid getFluid(ItemStack stack) {
        if (getFluidLevel(stack) == 0 || !(stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE))) {
            return Fluids.EMPTY;
        }
        Identifier fluidId = Identifier.tryParse(stack.getSubNbt("fluid").getString("id"));
        return fluidId == null ? Fluids.EMPTY : Registries.FLUID.getOrEmpty(fluidId).orElse(Fluids.EMPTY);
    }

    default int getFluidLevel(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE) ? stack.getSubNbt("fluid").getInt("level") : 0;
    }

    default FluidState getFluidState(ItemStack stack) {
        return getFluid(stack).getDefaultState().withIfExists(FlowableFluid.LEVEL, getFluidLevel(stack));
    }

    default ItemStack drain(ItemStack stack, int amount) {
        ItemStack removed = setLevel(stack.copy(), amount);
        setLevel(stack, getFluidLevel(stack) - amount);
        return removed;
    }

    default ItemStack setLevel(ItemStack stack, int level) {
        level = MathHelper.clamp(level, 0, getMaxCapacity());
        if (level == 0) {
            return setFluid(stack, Fluids.EMPTY);
        }
        stack.getOrCreateSubNbt("fluid").putInt("level", level);
        return stack;
    }

    default ItemStack setFluid(ItemStack stack, Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            if (stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) {
                stack.getNbt().remove("fluid");
            }
        } else {
            stack.getOrCreateSubNbt("fluid").putString("id", Registries.FLUID.getId(fluid).toString());
        }

        return stack;
    }

    @Override
    default boolean canPour(ItemStack stack, ItemStack destination) {
        Fluid myFluid = getFluid(stack);
        return myFluid != Fluids.EMPTY
            && myFluid == getFluid(destination)
            && getFluidLevel(destination) < getMaxCapacity(destination);
    }

    @Override
    default boolean canReceivePour(ItemStack stack, ItemStack src) {
        return canPour(src, stack);
    }
}
