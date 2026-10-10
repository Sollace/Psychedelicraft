package ivorius.psychedelicraft.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface Untyped {
    @SuppressWarnings("unchecked")
    static <A, B> B cast(A a) {
        return (B)a;
    }

    static <A, B> Optional<B> castOptional(Optional<A> a) {
        return cast(a);
    }

    static <A, B> Function<A, B> constant(B b) {
        return a -> b;
    }

    static <T> T applyIf(boolean condition, T t, UnaryOperator<T> action) {
        return condition ? action.apply(t) : t;
    }

    static <T> T recursive(Function<Supplier<T>, T> functor) {
        var holder = new Object() {
            T value;
        };
        holder.value = functor.apply(() -> holder.value);
        return holder.value;
    }
}
