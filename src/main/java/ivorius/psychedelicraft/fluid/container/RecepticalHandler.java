package ivorius.psychedelicraft.fluid.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;

public class RecepticalHandler {
    private static final List<Pair<Predicate<ItemStack>, RecepticalHandler>> REGISTRY = new ArrayList<>();
    private static final RecepticalHandler DEFAULT = new RecepticalHandler();

    public static RecepticalHandler get(ItemStack stack) {
        return REGISTRY.stream().filter(pair -> pair.getFirst().test(stack)).map(Pair::getSecond).findFirst().orElse(DEFAULT);
    }

    public static void register(Item item, RecepticalHandler handler) {
        register(i -> i.isOf(item), handler);
    }

    public static void register(TagKey<Item> tag, RecepticalHandler handler) {
        register(i -> i.isIn(tag), handler);
    }

    public static void register(Predicate<ItemStack> item, RecepticalHandler handler) {
        REGISTRY.add(new Pair<>(item, handler));
    }

    public static void registerPair(Item empty, Item filled, RecepticalHandler handler) {
        register(i -> i.isOf(empty) || i.isOf(filled), handler);
    }

    public static void registerPair(Item empty, Item filled) {
        registerPair(empty, filled, new RecepticalHandler() {
            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                return changeStackType(item, filled);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return changeStackType(item, empty);
            }
        });
    }

    public static ItemStack changeStackType(ItemStack item, Item newType) {
        return item.getItem() == newType ? item : item.withItem(newType);
    }

    public ItemStack toFilled(ItemStack item, ItemFluids contents) {
        return item;
    }

    public ItemStack toEmpty(ItemStack item) {
        return item;
    }

    static {
        register(stack -> stack.isIn(ConventionalItemTags.BUCKETS) || stack.isIn(ConventionalItemTags.EMPTY_BUCKETS) || stack.isOf(PSItems.FILLED_BUCKET), new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBuckets = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bucket";
                return Registries.ITEM.getIds().stream().filter(id -> id.getPath().equals(path)).findFirst().map(Registries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                if (contents.amount() < FluidVolumes.BUCKET) {
                    return changeStackType(item, PSItems.FILLED_BUCKET);
                }
                return changeStackType(item, filledBuckets.apply(contents.fluid()).orElse(PSItems.FILLED_BUCKET));
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return changeStackType(item, Items.BUCKET);
            }
        });
        registerPair(Items.BOWL, PSItems.FILLED_BOWL);
        register(stack -> stack.getItem() instanceof GlassBottleItem || stack.getItem() instanceof PotionItem || stack.getItem() == PSItems.FILLED_GLASS_BOTTLE, new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBottles = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bottle";
                String altPath = fluid.getId().getPath() + "_glass_bottle";
                return Registries.ITEM.getIds().stream().filter(id -> id.getPath().equals(path) || id.getPath().equals(altPath)).findFirst().map(Registries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                if (contents.amount() < FluidVolumes.BOTTLE) {
                    return changeStackType(item, PSItems.FILLED_GLASS_BOTTLE);
                }
                if (contents.fluid() == SimpleFluid.forVanilla(Fluids.WATER)) {
                    return PotionContentsComponent.createStack(Items.POTION, Potions.WATER);
                }
                return changeStackType(item, filledBottles.apply(contents.fluid()).orElse(PSItems.FILLED_GLASS_BOTTLE));
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return changeStackType(item, Items.GLASS_BOTTLE);
            }
        });
    }
}
