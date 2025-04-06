package ivorius.psychedelicraft.util.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public interface ItemSubPredicate<T> {
    Map<Identifier, Type<?>> REGISTRY = new HashMap<>();
    Map<Type<?>, Identifier> REVERSE_REGISTRY = new HashMap<>();

    ComponentType<T> getComponentType();

    boolean test(ItemStack stack, T component);

    static <V, T extends ItemSubPredicate<V>> Type<T> register(Identifier id, Type<T> type) {
        REGISTRY.put(id, type);
        REVERSE_REGISTRY.put(type, id);
        return type;
    }

    static List<ItemSubPredicate<?>> readNbt(NbtCompound predicates) {
        List<ItemSubPredicate<?>> list = new ArrayList<>();
        predicates.getKeys().forEach(key -> {
            Identifier id = new Identifier(key);
            Optional.ofNullable(REGISTRY.get(id))
                .map(type -> type.codec().decode(NbtOps.INSTANCE, predicates.get(key)).result().orElse(null))
                .map(pair -> pair.getFirst())
                .ifPresent(list::add);
        });
        return list;
    }

    record Type<T extends ItemSubPredicate<?>>(Codec<T> codec) {}

    public class PredicateBuilder {
        private final NbtCompound nbt = new NbtCompound();

        public <T extends ItemSubPredicate<?>> PredicateBuilder add(Type<T> type, T predicate) {
            type.codec().encodeStart(NbtOps.INSTANCE, predicate).result().ifPresent(data -> {
                nbt.put(REVERSE_REGISTRY.get(type).toString(), data);
            });
            return this;
        }

        public NbtCompound build() {
            NbtCompound comp = new NbtCompound();
            comp.put("psychedelicraft:sub_predicates", nbt);
            return comp;
        }
    }
}
