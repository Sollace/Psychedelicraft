package ivorius.psychedelicraft.util;

import net.minecraft.util.math.random.Random;

public interface Pool<T> {

    @SafeVarargs
    static <T> Pool<T> create(T... options) {
        return rng -> options[rng.nextInt(options.length)];
    }

    T get(Random rng);
}
