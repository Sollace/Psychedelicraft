package ivorius.psychedelicraft.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.alcohol.TickRate;
import net.minecraft.registry.RegistryKey;

public record TickRates (Map<RegistryKey<SimpleFluid>, TickRate> values) {
    public static final Codec<TickRates> CODEC = Codec.unboundedMap(
        RegistryKey.createCodec(SimpleFluid.REGISTRY.getKey()),
        TickRate.CODEC
    ).xmap(Function.identity(), values -> {
        var mutable = new HashMap<>(TickRate.getDefaults());
        mutable.putAll(values);
        return Map.copyOf(mutable);
    }).xmap(TickRates::new, TickRates::values);
}
