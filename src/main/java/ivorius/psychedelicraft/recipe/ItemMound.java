package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ItemMound implements NbtSerialisable {
    private final List<Item> indexes = new ArrayList<>();
    private final Object2IntMap<Item> items = new Object2IntOpenHashMap<>();

    public ItemMound() {

    }

    public ItemMound(ItemMound original) {
        items.putAll(original.items);
        indexes.addAll(original.indexes);
    }

    public ItemMound(NbtCompound compound, WrapperLookup lookup) {
        fromNbt(compound, lookup);
    }

    public void addStack(ItemStack stack) {
        add(stack.getItem(), stack.getCount());
    }

    public ItemStack removeStack(int index, int amount) {
        if (index >= 0 && index < indexes.size()) {
            Item item = indexes.get(index);
            amount = Math.min(items.getInt(item), amount);
            remove(item, amount);
            return new ItemStack(item, amount);
        }
        return ItemStack.EMPTY;
    }

    public void add(Item item, int amount) {
        items.compute(item, (i, count) -> (count == null ? 0 : count) + amount);
        if (!indexes.contains(item)) {
            indexes.add(item);
        }
    }

    public void remove(Item item, int amount) {
        if (items.compute(item, (i, count) -> count <= amount ? null : (count - amount)) == null) {
            indexes.remove(item);
        }
    }

    public boolean removeWhere(@Nullable Predicate<ItemStack> predicate, int max) {
        for (Item item : items.keySet().stream().filter(i -> predicate == null || predicate.test(i.getDefaultStack())).toList()) {
            int available = Math.min(max, items.getInt(item));
            max -= available;
            remove(item, available);
            if (max <= 0) {
                return true;
            }
        }
        return false;
    }

    public int countMatches(@Nullable Predicate<ItemStack> predicate) {
        return items.object2IntEntrySet().stream()
                .filter(i -> predicate == null || predicate.test(i.getKey().getDefaultStack()))
                .mapToInt(Object2IntMap.Entry::getIntValue)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Object2IntMap<Item> getCounts() {
        return items;
    }

    public DefaultedList<ItemStack> convertToItemStacks() {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size());
        items.forEach((item, count) -> {
            while (count > 0) {
                ItemStack stack = new ItemStack(item, Math.min(item.getMaxCount(), count));
                count-= stack.getCount();
                stacks.add(stack);
            }
        });
        return stacks;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        items.forEach((item, count) -> {
            compound.putInt(Registries.ITEM.getId(item).toString(), count);
        });
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        items.clear();
        indexes.clear();
        compound.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).map(Registries.ITEM::get)
                .filter(Objects::nonNull)
                .ifPresent(item -> add(item, compound.getInt(key)));
        });
    }
}
