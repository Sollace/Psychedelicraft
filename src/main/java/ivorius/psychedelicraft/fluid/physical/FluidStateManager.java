package ivorius.psychedelicraft.fluid.physical;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import ivorius.psychedelicraft.item.component.ItemFluids;
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

    public void writeAttributes(State<?, ?> state, Map<String, Integer> attributes) {
        for (FluidProperty<?> property : properties) {
            property.write(state, attributes);
        }
    }

    public <O, S extends State<O, S>> S readAttributes(S state, ItemFluids stack) {
        for (FluidProperty<?> property : properties) {
            state = property.read(state, stack);
        }
        return state;
    }

    private <O, S extends State<O, S>, T extends Comparable<T>> S copyStateValue(State<?, ?> from, S to, Property<T> property) {
        return from.getOrEmpty(property).map(v -> to.withIfExists(property, v)).orElse(to);
    }

    public record FluidProperty<T extends Comparable<T>>(
            Property<T> property,
            BiConsumer<Map<String, Integer>, T> writer,
            Function<ItemFluids, T> reader) {
        void write(State<?, ?> state, Map<String, Integer> attributes) {
            writer.accept(attributes, state.get(property));
        }

        <O, S extends State<O, S>> S read(S state, ItemFluids stack) {
            return state.with(property, reader.apply(stack));
        }
    }
}
