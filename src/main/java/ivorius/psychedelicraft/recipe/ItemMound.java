package ivorius.psychedelicraft.recipe;

import java.util.Objects;
import java.util.Optional;

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
    private final Object2IntMap<Item> items = new Object2IntOpenHashMap<>();

    public ItemMound() {

    }

    public ItemMound(ItemMound original) {
        items.putAll(original.items);
    }

    public void addStack(ItemStack stack) {
        add(stack.getItem(), stack.getCount());
    }

    public void add(Item item, int amount) {
        items.compute(item, (i, count) -> (count == null ? 0 : count) + amount);
    }

    public void remove(Item item, int amount) {
        items.compute(item, (i, count) -> count <= amount ? null : (count - amount));
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
        compound.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).map(Registries.ITEM::get).filter(Objects::nonNull).ifPresent(item -> {
                items.put(item, compound.getInt(key));
            });
        });
    }
}
