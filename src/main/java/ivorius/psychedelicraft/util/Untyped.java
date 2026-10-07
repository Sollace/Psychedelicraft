package ivorius.psychedelicraft.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Untyped {
    @SuppressWarnings("unchecked")
    static <A, B> B cast(A a) {
        return (B)a;
    }

    static <A, B> Optional<B> castOptional(Optional<A> a) {
        return cast(a);
    }

    static <T> T recursive(Function<Supplier<T>, T> functor) {
        var holder = new Object() {
            T value;
        };
        holder.value = functor.apply(() -> holder.value);
        return holder.value;
    }
}
