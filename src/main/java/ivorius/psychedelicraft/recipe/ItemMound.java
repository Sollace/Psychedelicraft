package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ItemMound implements NbtSerialisable {
    private final List<Item> indexes = new ArrayList<>();
    private final Map<Item, Entry> items = new HashMap<>();

    public ItemMound() {

    }

    public ItemMound(ItemMound original) {
        synchronized (original) {
            indexes.addAll(original.indexes);
            original.items.entrySet().forEach(entry -> items.put(entry.getKey(), new Entry(entry.getValue())));
        }
    }

    public ItemMound(NbtCompound compound) {
        fromNbt(compound);
    }

    public synchronized void addStack(ItemStack stack) {
        Entry entry = items.computeIfAbsent(stack.getItem(), Entry::new);
        entry.push(stack.getNbt(), stack.getCount());
        if (!indexes.contains(stack.getItem())) {
            indexes.add(stack.getItem());
        }
    }

    public synchronized ItemStack removeStack(int index, int amount) {
        if (index >= 0 && index < indexes.size()) {
            ItemStack[] stack = { ItemStack.EMPTY };
            items.compute(indexes.get(index), (i, entry) -> entry == null ? null : entry.remove(amount, stack));
            return stack[0];
        }
        return ItemStack.EMPTY;
    }

    public synchronized boolean removeWhere(@Nullable Predicate<ItemStack> predicate, int max) {
        var iter = items.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next().getValue();
            if (predicate == null || predicate.test(entry.item.getDefaultStack())) {
                int available = Math.min(max, entry.count);
                max -= available;
                if (entry.removeWhere(null, available) == null) {
                    indexes.remove(entry.item);
                    iter.remove();
                }

                if (max <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized int countMatches(@Nullable Predicate<ItemStack> predicate) {
        return items.values().stream()
                .mapToInt(i -> i.countMatches(predicate))
                .sum();
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized ItemStack getStack(int index) {
        ItemStack[] stack = {ItemStack.EMPTY};
        Entry entry = items.get(this.indexes.get(index));
        entry.popSingle(stack, false);
        stack[0].setCount(entry.count);
        return stack[0];
    }

    public synchronized int getCount(Item item) {
        Entry entry = items.get(item);
        return entry == null ? 0 : entry.count;
    }

    public synchronized DefaultedList<ItemStack> convertToItemStacks() {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size());
        items.forEach((item, entry) -> entry.toItemStacks(stacks));
        return stacks;
    }

    public synchronized void clear() {
        items.clear();
        indexes.clear();
    }

    @Override
    public synchronized void toNbt(NbtCompound compound) {
        items.forEach((item, count) -> {
            compound.put(Registries.ITEM.getId(item).toString(), count.toNbt());
        });
    }

    @Override
    public synchronized void fromNbt(NbtCompound compound) {
        clear();
        compound.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).map(Registries.ITEM::get)
                .filter(Objects::nonNull)
                .ifPresent(item -> {
                    Entry entry = items.computeIfAbsent(item, Entry::new);
                    if (!indexes.contains(item)) {
                        indexes.add(item);
                    }
                    if (compound.contains(key, NbtElement.COMPOUND_TYPE)) {
                        items.computeIfAbsent(item, Entry::new).fromNbt(compound.getCompound(key));
                    } else if (compound.contains(key, NbtElement.INT_TYPE)) {
                        entry.push(new NbtCompound(), compound.getInt(key));
                    }
                });
        });
    }

    private final class Entry {
        private static final Codec<Map<NbtCompound, ComponentStack>> COMPONENTS_CODEC = ComponentStack.CODEC.listOf().xmap(
                list -> list.stream().collect(Collectors.toUnmodifiableMap(p -> p.changes, p -> p, (a, b) -> b)),
                map -> map.entrySet().stream().map(e -> e.getValue()).toList()
        );

        private int count;
        private final Item item;
        private final Map<NbtCompound, ComponentStack> components = new HashMap<>();

        Entry(Item item) {
            this.item = item;
        }

        Entry(Entry entry) {
            this.item = entry.item;
            this.count = entry.count;
            this.components.putAll(entry.components);
        }

        public void fromNbt(NbtCompound nbt) {
            count = nbt.getInt("count");
            components.clear();
            NbtSerialisable.get(nbt, "components", COMPONENTS_CODEC).ifPresent(components::putAll);
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("count", count);
            NbtSerialisable.put(nbt, "components", COMPONENTS_CODEC, components);
            return nbt;
        }

        public void toItemStacks(DefaultedList<ItemStack> stacks) {
            int outputted = 0;
            for (var entry : components.values()) {
                outputted += entry.count;
                ItemStack stack = entry.getStack(item);
                int maxCount = stack.getMaxCount();
                int stackCount = entry.count / maxCount;
                for (int i = 0; i < stackCount; i++) {
                    stacks.add(stack.copyWithCount(maxCount));
                }
                int last = entry.count % maxCount;
                if (last > 0) {
                    stacks.add(stack.copyWithCount(maxCount));
                }
            }
            while (outputted++ < count) {
                stacks.add(item.getDefaultStack());
            }
        }

        public int countMatches(@Nullable Predicate<ItemStack> predicate) {
            if (predicate == null) {
                return count;
            }

            int testCount = 0;
            int matchCount = 0;
            for (var entry : components.values()) {
                testCount += entry.count;
                if (predicate.test(entry.getStack(item))) {
                    matchCount += entry.count;
                }
            }
            if (predicate.test(item.getDefaultStack())) {
                matchCount += count - testCount;
            }

            return matchCount;
        }

        public Entry removeWhere(@Nullable Predicate<ItemStack> predicate, int count) {
            int matchCount = 0;
            int testCount = 0;
            if (this.count == 0) {
                components.clear();
            } else {
                var iter = components.entrySet().iterator();
                while (iter.hasNext() && count > 0) {
                    var entry = iter.next().getValue();
                    int subtracted = Math.min(count, entry.count);
                    testCount += subtracted;
                    if (predicate != null && !predicate.test(entry.getStack(item))) {
                        continue;
                    }
                    count -= subtracted;
                    matchCount += subtracted;
                    if (subtracted <= entry.count) {
                        iter.remove();
                    } else {
                        entry.count -= subtracted;
                    }
                }
                if (predicate == null || predicate.test(item.getDefaultStack())) {
                    matchCount += this.count - testCount;
                }
                this.count = Math.max(0, this.count - matchCount);
            }

            return this.count <= 0 ? null : this;
        }

        public void push(NbtCompound changes, int count) {
            this.count += count;
            if (!changes.isEmpty()) {
                components.compute(changes, (i, c) -> {
                    if (c == null) {
                        return new ComponentStack(i, count);
                    }
                    c.count += count;
                    return c;
                });
            }
        }

        public Entry remove(int amount, ItemStack[] outStack) {
            amount = Math.min(count, amount);

            if (amount <= 0) {
                outStack[0] = ItemStack.EMPTY;
                return null;
            }
            if (amount == 1) {
                return popSingle(outStack, true);
            }

            var entry = components.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(i -> -i.count))
                .findFirst().orElse(null);
            if (entry == null) {
                outStack[0] = item.getDefaultStack();
            } else {
                amount = Math.min(amount, entry.count);
                count -= amount;
                if (count <= 0) {
                    items.remove(item);
                }

                outStack[0] = entry.getStack(item).copy();
                if (amount >= entry.count) {
                    components.remove(entry.changes);
                } else {
                    entry.count -= amount;
                }
            }
            return count <= 0 ? null : this;
        }

        public Entry popSingle(ItemStack[] outStack, boolean remove) {
            if (remove) {
                count = Math.max(0, count);
            }
            var iter = components.entrySet().iterator();
            if (!iter.hasNext()) {
                outStack[0] = item.getDefaultStack();
            } else {
                var entry = iter.next().getValue();
                if (remove) {
                    if (entry.count <= 1) {
                        iter.remove();
                    } else {
                        entry.count--;
                    }
                }

                outStack[0] = entry.getStack(item).copy();
            }
            return count <= 0 ? null : this;
        }

        private static final class ComponentStack {
            private static final Codec<ComponentStack> CODEC = RecordCodecBuilder.create(i -> i.group(
                    NbtCompound.CODEC.fieldOf("changes").forGetter(s -> s.changes),
                    Codec.INT.fieldOf("count").forGetter(s -> s.count)
            ).apply(i, ComponentStack::new));

            public final NbtCompound changes;
            public int count;

            private ItemStack cachedStack;

            ComponentStack(NbtCompound changes, int count) {
                this.changes = changes.copy();
                this.count = count;
            }

            public ItemStack getStack(Item item) {
                if (cachedStack == null) {
                    cachedStack = item.getDefaultStack();
                    if (!changes.isEmpty()) {
                        cachedStack.setNbt(changes);
                    }
                }
                return cachedStack;
            }
        }
    }
}
