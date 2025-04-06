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
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.util.compat.StackCompat;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
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
                return applyFluid(changeStackType(item, filled), contents);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, empty), ItemFluids.EMPTY);
            }
        });
    }

    public static ItemStack changeStackType(ItemStack item, Item newType) {
        return item.getItem() == newType ? item : StackCompat.withItem(item, newType);
    }

    public static ItemStack applyFluid(ItemStack stack, ItemFluids contents) {
        if (contents.isEmpty()) {
            StackCompat.remove(stack, PSComponents.FLUIDS);
        } else {
            StackCompat.set(stack, PSComponents.FLUIDS, contents);
        }
        return stack;
    }

    public ItemStack toFilled(ItemStack item, ItemFluids contents) {
        return applyFluid(item.copy(), contents);
    }

    public ItemStack toEmpty(ItemStack item) {
        return applyFluid(item.copy(), ItemFluids.EMPTY);
    }

    static {
        register(stack -> stack.getItem() instanceof BucketItem || stack.isOf(PSItems.FILLED_BUCKET), new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBuckets = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bucket";
                return Registries.ITEM.getIds().stream().filter(id -> id.getPath().equals(path)).findFirst().map(Registries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                Item newType = contents.amount() < FluidVolumes.BUCKET || !contents.isBaseForm()
                        ? PSItems.FILLED_BUCKET
                        : filledBuckets.apply(contents.fluid()).orElse(PSItems.FILLED_BUCKET);
                return applyFluid(changeStackType(item, newType), newType == PSItems.FILLED_BUCKET ? contents : ItemFluids.EMPTY);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, Items.BUCKET), ItemFluids.EMPTY);
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
                if (contents.amount() < FluidVolumes.GLASS_BOTTLE) {
                    return applyFluid(changeStackType(item, PSItems.FILLED_GLASS_BOTTLE), contents);
                }
                if (contents.isOf(Fluids.WATER)) {
                    return PotionUtil.setPotion(Items.POTION.getDefaultStack(), Potions.WATER);
                }
                Item newType = contents.amount() < FluidVolumes.GLASS_BOTTLE || !contents.isBaseForm()
                        ? PSItems.FILLED_GLASS_BOTTLE
                        : filledBottles.apply(contents.fluid()).orElse(PSItems.FILLED_GLASS_BOTTLE);
                return applyFluid(changeStackType(item, newType), newType == PSItems.FILLED_GLASS_BOTTLE ? contents : ItemFluids.EMPTY);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, Items.GLASS_BOTTLE), ItemFluids.EMPTY);
            }
        });
    }
}
