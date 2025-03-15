package ivorius.psychedelicraft.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record FluidIngredient (Optional<SimpleFluid> fluid, Optional<Integer> level, Map<String, Integer> attributes) {
    public static final FluidIngredient EMPTY = new FluidIngredient(Optional.empty(), Optional.empty(), Map.of());
    public static final Codec<FluidIngredient> CODEC = Codec.either(
            SimpleFluid.CODEC.xmap(fluid -> new FluidIngredient(Optional.of(fluid), Optional.empty(), Map.of()), i -> i.fluid().orElse(PSFluids.EMPTY)),
            RecordCodecBuilder.<FluidIngredient>create(instance -> instance.group(
                    SimpleFluid.CODEC.optionalFieldOf("fluid").forGetter(FluidIngredient::fluid),
                    Codec.INT.optionalFieldOf("level").forGetter(FluidIngredient::level),
                    Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("attributes", Map.of()).forGetter(FluidIngredient::attributes)
            ).apply(instance, FluidIngredient::new))
        ).xmap(RecipeUtils::iDontCareWhich, Either::right);
    public static final PacketCodec<RegistryByteBuf, FluidIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(SimpleFluid.PACKET_CODEC), FluidIngredient::fluid,
            PacketCodecs.optional(PacketCodecs.INTEGER), FluidIngredient::level,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.INTEGER), FluidIngredient::attributes,
            FluidIngredient::new
    );

    public FluidIngredient {
        fluid = fluid.filter(f -> !f.isEmpty());
    }

    public boolean test(ItemStack stack) {
        return test(ItemFluids.of(stack));
    }

    public boolean test(ItemFluids fluids) {
        boolean result = true;
        result &= fluid.isEmpty() || fluids.fluid() == fluid.get();
        result &= attributes.isEmpty() || attributes.equals(fluids.attributes());
        result &= level.isEmpty() || fluids.amount() >= level.get();
        return result;
    }

    public ItemFluids getAsItemFluid(int capacity) {
        return ItemFluids.create(fluid.orElse(PSFluids.EMPTY), level.orElse(capacity), attributes);
    }

    public boolean isEmpty() {
        return fluid.isEmpty() && level.isEmpty() && attributes.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Optional<SimpleFluid> fluid = Optional.empty();
        private Optional<Integer> level = Optional.empty();
        private final Map<String, Integer> attributes = new HashMap<>();

        public Builder fluid(SimpleFluid fluid) {
            this.fluid = Optional.of(fluid);
            return this;
        }

        public Builder level(int level) {
            this.level = Optional.of(level);
            return this;
        }

        public Builder attribute(String attribute, int value) {
            this.attributes.put(attribute, value);
            return this;
        }

        public FluidIngredient build() {
            return new FluidIngredient(fluid, level, Map.copyOf(attributes));
        }
    }
}

