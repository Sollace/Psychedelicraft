package ivorius.psychedelicraft.util.compat;

import com.mojang.datafixers.util.Either;

public interface EitherCompat {
    static <T> T unwrap(Either<T, T> either) {
        return either.left().or(either::right).orElseThrow();
    }
}
