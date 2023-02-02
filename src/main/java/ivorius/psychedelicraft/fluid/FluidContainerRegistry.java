package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtElement;

public class FluidContainerRegistry {
    private static final Map<Item, Supplier<FluidContainer>> ENTRIES = new HashMap<>();
    private static final Map<Item, Map<SimpleFluid, Item>> REFILL_MAPPING = new HashMap<>();

    public static Optional<FluidContainer> getContainer(Item item) {
        return Optional.ofNullable(ENTRIES.get(item)).map(Supplier::get);
    }

    public static void registerRefillMapping(Item emptyForm, SimpleFluid fluid, Item filledForm) {
        REFILL_MAPPING.computeIfAbsent(emptyForm, i -> new HashMap<>()).put(fluid, filledForm);
    }

    public static void registerFillableContainer(Function<Item, FluidContainer> container, Item... items) {
        for (Item item : items ) {
            ENTRIES.put(item, Suppliers.memoize(() -> container.apply(item)));
        }
    }

    public static void registerFillableContainer(Item emptyForm, int capacity, SimpleFluid fluid, Item... items) {
        registerFillableContainer(i -> {
            return new FluidContainer() {
                @Override
                public Item asItem() {
                    return i;
                }

                @Override
                public Item asEmpty() {
                    return emptyForm;
                }

                @Override
                public Item asFilled(SimpleFluid fluid) {
                    return REFILL_MAPPING.getOrDefault(asEmpty(), Map.of()).getOrDefault(fluid, asEmpty());
                }

                @Override
                public int getMaxCapacity() {
                    return capacity;
                }

                @Override
                public int getLevel(ItemStack stack) {
                    if (stack.getItem() == asEmpty()) {
                        return 0;
                    }
                    return stack.getNbt() != null
                        && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)
                        && stack.getSubNbt("fluid").contains("level", NbtElement.INT_TYPE) ? stack.getSubNbt("fluid").getInt("level") : capacity;
                }

                @Override
                public SimpleFluid getFluid(ItemStack stack) {
                    if (stack.getItem() == asEmpty()) {
                        return FluidContainer.super.getFluid(stack);
                    }
                    return fluid;
                }
            };
        }, items);
    }

    static {
        registerFillableContainer(Items.BUCKET, FluidVolumes.BUCKET, SimpleFluid.forVanilla(Fluids.WATER), Items.WATER_BUCKET, Items.COD_BUCKET, Items.SALMON_BUCKET, Items.TADPOLE_BUCKET, Items.TROPICAL_FISH_BUCKET);
        registerFillableContainer(Items.BUCKET, FluidVolumes.BUCKET, SimpleFluid.forVanilla(Fluids.LAVA), Items.LAVA_BUCKET);
        registerFillableContainer(Items.BUCKET, FluidVolumes.BUCKET, PSFluids.MILK, Items.MILK_BUCKET);
        registerFillableContainer(Items.BUCKET, FluidVolumes.BUCKET, PSFluids.EMPTY, Items.BUCKET);
        registerFillableContainer(Items.BOWL, FluidVolumes.BOWL, PSFluids.EMPTY, Items.BOWL);
        registerFillableContainer(Items.GLASS_BOTTLE, FluidVolumes.BOTTLE, SimpleFluid.forVanilla(Fluids.WATER), Items.POTION);
        registerFillableContainer(Items.GLASS_BOTTLE, FluidVolumes.BOTTLE, PSFluids.HONEY, Items.HONEY_BOTTLE);
        registerFillableContainer(Items.GLASS_BOTTLE, FluidVolumes.BOTTLE, PSFluids.EMPTY, Items.GLASS_BOTTLE);
        registerRefillMapping(Items.BUCKET, SimpleFluid.forVanilla(Fluids.WATER), Items.WATER_BUCKET);
        registerRefillMapping(Items.BUCKET, SimpleFluid.forVanilla(Fluids.LAVA), Items.LAVA_BUCKET);
        registerRefillMapping(Items.GLASS_BOTTLE, SimpleFluid.forVanilla(Fluids.WATER), Items.POTION);
        registerRefillMapping(Items.GLASS_BOTTLE, PSFluids.HONEY, Items.HONEY_BOTTLE);
    }
}
