package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public record TickRate (int ticksPerFermentation, int ticksPerDistillation, int ticksPerMaturation, int ticksUntilAcetification) {
    public static final int MINUTE = 20 * 60;
    public static final TickRate DEFAULT = ofMinutes(40, 40, 30, 30);
    public static final Codec<TickRate> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("ticksPerFermentation").forGetter(TickRate::ticksPerFermentation),
            Codec.INT.fieldOf("ticksPerDistillation").forGetter(TickRate::ticksPerDistillation),
            Codec.INT.fieldOf("ticksPerMaturation").forGetter(TickRate::ticksPerMaturation),
            Codec.INT.fieldOf("ticksUntilAcetification").forGetter(TickRate::ticksUntilAcetification)
    ).apply(i, TickRate::new));

    public static TickRate ofMinutes(int f, int d, int m, int a) {
        return new TickRate(f * MINUTE, d * MINUTE, m * MINUTE, a * MINUTE);
    }

    public static Map<RegistryKey<SimpleFluid>, TickRate> getDefaults() {
        return SimpleFluid.REGISTRY.streamEntries()
                .filter(i -> i.value() instanceof TickRate.Tickable && i.getKey().isPresent())
                .collect(Collectors.<RegistryEntry<SimpleFluid>, RegistryKey<SimpleFluid>, TickRate>toMap(
                        i -> i.getKey().orElseThrow(),
                        i -> ((TickRate.Tickable)i.value()).getDefaultTickRate()
                ));
    }

    public interface Tickable {
        TickRate getDefaultTickRate();

        @SuppressWarnings("deprecation")
        default TickRate getTickRate() {
            return Psychedelicraft.getConfig().fluidAttributes.get().values().getOrDefault(((SimpleFluid)this).getRegistryEntry().getKey().orElseThrow(), getDefaultTickRate());
        }
    }
}
