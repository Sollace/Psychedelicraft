package ivorius.psychedelicraft.fluid.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;

public interface RecepticalHandler {
    List<Pair<Predicate<ItemStack>, RecepticalHandler>> REGISTRY = new ArrayList<>();
    RecepticalHandler DEFAULT = new RecepticalHandler() {};

    static RecepticalHandler get(ItemStack stack) {
        return REGISTRY.stream().filter(pair -> pair.getFirst().test(stack)).map(Pair::getSecond).findFirst().orElse(DEFAULT);
    }

    static void register(Item item, RecepticalHandler handler) {
        register(i -> i.isOf(item), handler);
    }

    static void register(TagKey<Item> tag, RecepticalHandler handler) {
        register(i -> i.isIn(tag), handler);
    }

    static void register(Predicate<ItemStack> item, RecepticalHandler handler) {
        REGISTRY.add(new Pair<>(item, handler));
    }

    static void registerPair(Item empty, Item filled, RecepticalHandler handler) {
        register(i -> i.isOf(empty) || i.isOf(filled), handler);
    }

    static void registerPair(Item empty, Item filled) {
        registerPair(empty, filled, new RecepticalHandler() {
            @Override
            public ItemStack toFilled(Item item, ItemFluids contents) {
                return filled.getDefaultStack();
            }

            @Override
            public ItemStack toEmpty(Item item) {
                return empty.getDefaultStack();
            }
        });
    }

    default ItemStack toFilled(Item item, ItemFluids contents) {
        return item.getDefaultStack();
    }

    default ItemStack toEmpty(Item item) {
        return item.getDefaultStack();
    }
}
