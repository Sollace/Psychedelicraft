package ivorius.psychedelicraft.util;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

public interface Compat119 {

    static Stream<JsonElement> stream(JsonArray array) {
        return StreamSupport.stream(Spliterators.spliterator(array.iterator(), array.size(), Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    static ItemStack copyWithCount(ItemStack stack, int count) {
        stack = stack.copy();
        stack.setCount(count);
        return stack;
    }

    static Identifier withPath(Identifier id, Function<String, String> pathChange) {
        return new Identifier(id.getNamespace(), pathChange.apply(id.getPath()));
    }

    static Identifier withPrefixedPath(Identifier id, String prefix) {
        return withPath(id, p -> prefix + p);
    }

    static <O, S extends State<O, S>, V extends Comparable<V>> S withIfExists(S state, Property<V> property, V value) {
        return state.contains(property) ? state.with(property, value) : state;
    }

    class FabricItemGroup {
        private final FabricItemGroupBuilder builder;

        private FabricItemGroup(Identifier id) {
            builder = FabricItemGroupBuilder.create(id);
        }

        public static FabricItemGroup builder(Identifier id) {
            return new FabricItemGroup(id);
        }

        public FabricItemGroup icon(Supplier<ItemStack> icon) {
            builder.icon(icon);
            return this;
        }

        public FabricItemGroup entries(EntriesBuilder entries) {
            builder.appendItems((list, group) -> {
                entries.build(null, list::add, group == ItemGroup.SEARCH);
            });
            return this;
        }

        public ItemGroup build() {
            return builder.build();
        }

        public interface EntriesBuilder {
            void build(Object features, StacksList entries, boolean search);
        }
        public interface StacksList {
            default void add(Item item) {
                add(item.getDefaultStack());
            }

            void add(ItemStack stack);
        }
    }
}
