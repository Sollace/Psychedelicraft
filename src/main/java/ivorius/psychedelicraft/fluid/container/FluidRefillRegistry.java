package ivorius.psychedelicraft.fluid.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class FluidRefillRegistry {
    private static final Map<SimpleFluid, Map<Item, Item>> EMPTY_TO_FULL = new HashMap<>();
    private static final Map<Item, Item> FULL_TO_EMPTY = new HashMap<>();

    public static void register(Item empty, SimpleFluid fluid, Item filled) {
        EMPTY_TO_FULL.computeIfAbsent(fluid, f -> new HashMap<>()).put(empty, filled);
        FULL_TO_EMPTY.put(filled, empty);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack toFilled(ItemStack stack, ItemFluids fluids) {
        Item filled = EMPTY_TO_FULL.getOrDefault(fluids.fluid(), Map.of()).getOrDefault(stack.getItem(), stack.getItem());
        if (filled != stack.getItem()) {
            return new ItemStack(filled.getRegistryEntry(), stack.getCount(), stack.getComponentChanges());
        }
        return stack;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack toEmpty(ItemStack stack) {
        Item filled = FULL_TO_EMPTY.getOrDefault(stack.getItem(), stack.getItem());
        if (filled != stack.getItem()) {
            return new ItemStack(filled.getRegistryEntry(), stack.getCount(), stack.getComponentChanges());
        }
        return stack;
    }

    static {
        register(Items.BUCKET, SimpleFluid.forVanilla(Fluids.WATER), Items.WATER_BUCKET);
        register(Items.BUCKET, SimpleFluid.forVanilla(Fluids.LAVA), Items.LAVA_BUCKET);
        register(Items.BUCKET, PSFluids.MILK, Items.MILK_BUCKET);
        register(Items.GLASS_BOTTLE, SimpleFluid.forVanilla(Fluids.WATER), Items.POTION);
        register(Items.GLASS_BOTTLE, PSFluids.HONEY, Items.HONEY_BOTTLE);
        FULL_TO_EMPTY.put(PSItems.FILLED_BOWL, Items.BOWL);
        FULL_TO_EMPTY.put(PSItems.FILLED_BUCKET, Items.BUCKET);
        FULL_TO_EMPTY.put(PSItems.FILLED_GLASS_BOTTLE, Items.GLASS_BOTTLE);
        List.of(Items.COD_BUCKET, Items.SALMON_BUCKET, Items.TADPOLE_BUCKET, Items.TROPICAL_FISH_BUCKET).forEach(bucket -> {
            FULL_TO_EMPTY.put(bucket, Items.BUCKET);
        });
    }
}
