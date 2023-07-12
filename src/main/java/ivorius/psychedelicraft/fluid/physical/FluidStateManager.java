package ivorius.psychedelicraft.fluid.physical;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

public record FluidStateManager (Set<FluidProperty<?>> properties) {

    <O, S extends State<O, S>> void appendProperties(StateManager.Builder<O, S> builder) {
        properties.forEach(p -> builder.add(p.property));
    }

    <O, S extends State<O, S>> S copyStateValues(State<?, ?> from, S to) {
        for (FluidProperty<?> property : properties) {
            to = copyStateValue(from, to, property.property);
        }
        return to;
    }

    <O, S extends State<O, S>> S computeAverage(Stream<? extends State<?, ?>> states, S to) {
        return states.findFirst().map(state -> copyStateValues(state, to)).orElse(to);
    }

    public ItemStack writeStack(State<?, ?> state, ItemStack stack) {
        for (FluidProperty<?> property : properties) {
            stack = property.writeToStack(state, stack);
        }
        return stack;
    }

    public <O, S extends State<O, S>> S readStack(S state, ItemStack stack) {
        for (FluidProperty<?> property : properties) {
            state = property.readFromStack(state, stack);
        }
        return state;
    }

    private <O, S extends State<O, S>, T extends Comparable<T>> S copyStateValue(State<?, ?> from, S to, Property<T> property) {
        return from.getOrEmpty(property).map(v -> to.contains(property) ? to.with(property, v) : to).orElse(to);
    }

    public record FluidProperty<T extends Comparable<T>>(
            Property<T> property,
            BiFunction<ItemStack, T, ItemStack> writer,
            Function<ItemStack, T> reader) {
        ItemStack writeToStack(State<?, ?> state, ItemStack stack) {
            return writer.apply(stack, state.get(property));
        }

        <O, S extends State<O, S>> S readFromStack(S state, ItemStack stack) {
            return state.with(property, reader.apply(stack));
        }
    }
}
