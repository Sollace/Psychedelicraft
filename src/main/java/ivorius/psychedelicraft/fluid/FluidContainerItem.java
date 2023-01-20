package ivorius.psychedelicraft.fluid;

import java.util.*;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface FluidContainerItem extends ItemConvertible {
    Map<Item, FluidContainerItem> DYNAMIC_CONTAINERS = new HashMap<>();
    Map<TagKey<Item>, FluidContainerItem> DYNAMIC_CONTAINER_TAGS = new HashMap<>();
    FluidContainerItem UNLIMITED = new FluidContainerItem() {
        @Override
        public Item asItem() {
            return Items.STONE;
        }
        @Override
        public int getMaxCapacity() {
            return Integer.MAX_VALUE;
        }
    };
    FluidContainerItem BUCKET = create(Items.BUCKET, FluidVolumes.BUCKET);
    FluidContainerItem BOWL = create(Items.BOWL, FluidVolumes.BOWL);
    FluidContainerItem GLASS_BOTTLE = create(Items.GLASS_BOTTLE, FluidVolumes.GLASS_BOTTLE);
    FluidContainerItem CAULDRON = create(Items.CAULDRON, FluidVolumes.CAULDRON);

    static FluidContainerItem create(Item item, int capacity) {
        FluidContainerItem container = new FluidContainerItem() {
            @Override
            public Item asItem() {
                return item;
            }

            @Override
            public int getMaxCapacity() {
                return capacity;
            }
        };
        DYNAMIC_CONTAINERS.put(item, container);
        return container;
    }

    static FluidContainerItem of(ItemStack stack) {
        return of(stack, UNLIMITED);
    }

    static FluidContainerItem of(ItemStack stack, FluidContainerItem fallback) {
        return stack.getItem() instanceof FluidContainerItem c ? c : fallback;
    }

    int getMaxCapacity();

    default int getMaxCapacity(ItemStack stack) {
        return getMaxCapacity();
    }

    default float getFillPercentage(ItemStack stack) {
        return MathHelper.clamp(MathHelper.getLerpProgress(getFluidLevel(stack), 0, getMaxCapacity(stack)), 0, 1);
    }

    default ItemStack getDefaultStack(SimpleFluid fluid) {
        Item bucketItem = this != UNLIMITED || fluid.getFluidState(0).getFluid() == Fluids.EMPTY ? asItem() : fluid.getFluidState(0).getFluid().getBucketItem();
        return setLevel(setFluid(bucketItem.getDefaultStack(), fluid), getMaxCapacity());
    }

    default SimpleFluid getFluid(ItemStack stack) {
        if (!(stack.getNbt() != null && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) || getFluidLevel(stack) == 0) {
            return PSFluids.EMPTY;
        }
        return SimpleFluid.byId(Identifier.tryParse(stack.getSubNbt("fluid").getString("id")));
    }

    default int getFluidLevel(ItemStack stack) {
        return stack.getNbt() != null
                && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)
                && stack.getSubNbt("fluid").contains("id")
                && !SimpleFluid.byId(Identifier.tryParse(stack.getSubNbt("fluid").getString("id"))).isEmpty() ? stack.getSubNbt("fluid").getInt("level") : 0;
    }

    default ItemStack setLevel(ItemStack stack, int level) {
        level = MathHelper.clamp(level, 0, getMaxCapacity());
        if (level == 0) {
            return setFluid(stack, PSFluids.EMPTY);
        }
        stack.getOrCreateSubNbt("fluid").putInt("level", level);
        return stack;
    }

    default ItemStack setFluid(ItemStack stack, SimpleFluid fluid) {
        if (fluid.isEmpty()) {
            if (stack.getNbt() != null && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) {
                stack.getNbt().remove("fluid");
            }
        } else {
            stack.getOrCreateSubNbt("fluid").putString("id", fluid.getId().toString());
        }

        return stack;
    }

    /**
     * Removes a certain amount of fluid from the given stack and returns a
     * new stack containing the amount of fluid that was drained.
     */
    default ItemStack drain(ItemStack stack, int amount) {
        amount = Math.min(getFluidLevel(stack), amount);
        ItemStack removed = setLevel(stack.copy(), amount);
        setLevel(stack, getFluidLevel(stack) - amount);
        return removed;
    }

    /**
     * Adds fluid to this container.
     * Returns the remaining levels.
     */
    default int deposit(ItemStack stack, int amount, SimpleFluid fluid) {
        int newLevel = Math.min(getMaxCapacity(stack), getFluidLevel(stack) + amount);
        if (getFluid(stack).isEmpty()) {
            setFluid(stack, fluid);
        } else if (getFluid(stack) != fluid) {
            return amount;
        }
        setLevel(stack, newLevel);
        return amount - newLevel;
    }

    /**
     * Copies the fluid information from another stack to this one.
     */
    default ItemStack fill(ItemStack stack, ItemStack from) {
        stack = stack.copy();
        if (of(from).getFluid(from).isEmpty()) {
            return stack;
        }
        NbtCompound source = from.getNbt() != null && from.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE) ? from.getSubNbt("fluid") : new NbtCompound();
        SimpleFluid fluid = getFluid(stack);
        int level = getFluidLevel(stack) + of(from).getFluidLevel(from);
        stack.getOrCreateSubNbt("fluid").copyFrom(source);
        return setLevel(setFluid(stack, fluid), level);
    }
}
