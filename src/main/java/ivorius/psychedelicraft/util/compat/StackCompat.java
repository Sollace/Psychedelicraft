package ivorius.psychedelicraft.util.compat;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public interface StackCompat {
    static ItemStack splitUnlessCreative(ItemStack stack, int count, PlayerEntity player) {
        if (player.isCreative()) {
            return stack.copyWithCount(count);
        }
        return stack.split(count);
    }

    static void decrementUnlessCreative(ItemStack stack, int count, PlayerEntity player) {
        if (player.isCreative()) {
            return;
        }
        stack.decrement(count);
    }

    static ItemStack withItem(ItemStack stack, Item newItem) {
        ItemStack newStack = newItem.getDefaultStack().copyWithCount(stack.getCount());
        newStack.setNbt(stack.getNbt());
        return newStack;
    }

    @Nullable
    static void remove(ItemStack stack, ComponentType<?> componentType) {
        stack.removeSubNbt(componentType.id().toUnderscoreSeparatedString());
    }

    static <T> T set(ItemStack stack, ComponentType<T> componentType, T value) {
        if (stack.isEmpty()) {
            remove(stack, componentType);
            return value;
        }
        String key = componentType.id().toUnderscoreSeparatedString();

        componentType.codec().encodeStart(NbtOps.INSTANCE, value).result()
            .ifPresentOrElse(element -> stack.setSubNbt(key, element), () -> stack.removeSubNbt(key));
        return value;
    }

    @Nullable
    static <T> T get(ItemStack stack, ComponentType<T> componentType) {
        return get(stack.getItem(), stack.getNbt(), componentType);
    }

    @Nullable
    static <T> T get(TransferVariant<?> stack, ComponentType<T> componentType) {
        return get(stack.getObject() instanceof Item i ? i : null, stack.getNbt(), componentType);
    }

    @Nullable
    static <T> T get(@Nullable Item owner, @Nullable NbtCompound nbt, ComponentType<T> componentType) {
        if (nbt != null && owner != Items.AIR) {
            String key = componentType.id().toUnderscoreSeparatedString();

            if (nbt.contains(key)) {
                @Nullable
                Pair<T, ?> result = componentType.codec().decode(NbtOps.INSTANCE, nbt.get(key)).result().orElse(null);
                if (result != null) {
                    return result.getFirst();
                }
            }
        }

        return owner == null ? null : componentType.itemDefaults().get(owner);
    }

    @Nullable
    static <T> T getOrDefault(ItemStack stack, ComponentType<T> componentType, @Nullable T fallback) {
        T result = get(stack, componentType);
        return result == null ? fallback : result;
    }

    static boolean contains(ItemStack stack, ComponentType<?> componentType) {
        NbtCompound nbt = stack.getNbt();
        return (nbt != null
                && stack.getItem() != Items.AIR
                && nbt.contains(componentType.id().toUnderscoreSeparatedString())) || componentType.itemDefaults().containsKey(stack.getItem());
    }
}
