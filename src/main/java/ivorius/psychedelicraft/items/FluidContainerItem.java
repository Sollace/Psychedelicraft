package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.crafting.Pourable;
import ivorius.psychedelicraft.fluids.SimpleFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface FluidContainerItem extends Pourable {
    FluidContainerItem FLUID = () -> Integer.MAX_VALUE;

    static FluidContainerItem of(ItemStack stack) {
        return stack.getItem() instanceof FluidContainerItem c ? c : FLUID;
    }

    int getMaxCapacity();

    default int getMaxCapacity(ItemStack stack) {
        return getMaxCapacity();
    }

    default SimpleFluid getFluid(ItemStack stack) {
        if (getFluidLevel(stack) == 0 || !(stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE))) {
            return SimpleFluid.EMPTY;
        }
        Identifier fluidId = Identifier.tryParse(stack.getSubNbt("fluid").getString("id"));
        return fluidId == null ? SimpleFluid.EMPTY : SimpleFluid.REGISTRY.get(fluidId);
    }

    default int getFluidLevel(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE) ? stack.getSubNbt("fluid").getInt("level") : 0;
    }

    default ItemStack drain(ItemStack stack, int amount) {
        ItemStack removed = setLevel(stack.copy(), amount);
        setLevel(stack, getFluidLevel(stack) - amount);
        return removed;
    }

    default ItemStack setLevel(ItemStack stack, int level) {
        level = MathHelper.clamp(level, 0, getMaxCapacity());
        if (level == 0) {
            return setFluid(stack, SimpleFluid.EMPTY);
        }
        stack.getOrCreateSubNbt("fluid").putInt("level", level);
        return stack;
    }

    default ItemStack setFluid(ItemStack stack, SimpleFluid fluid) {
        if (fluid.isEmpty()) {
            if (stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) {
                stack.getNbt().remove("fluid");
            }
        } else {
            stack.getOrCreateSubNbt("fluid").putString("id", fluid.getId().toString());
        }

        return stack;
    }

    @Override
    default boolean canPour(ItemStack stack, ItemStack destination) {
        SimpleFluid myFluid = getFluid(stack);
        return !myFluid.isEmpty()
            && myFluid == getFluid(destination)
            && getFluidLevel(destination) < getMaxCapacity(destination);
    }

    @Override
    default boolean canReceivePour(ItemStack stack, ItemStack src) {
        return canPour(src, stack);
    }
}
