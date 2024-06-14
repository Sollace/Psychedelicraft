package ivorius.psychedelicraft.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;

public record FluidIngredient (Optional<SimpleFluid> fluid, Optional<Integer> level, Map<String, Integer> attributes) {
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

    public Ingredient toVanillaIngredient(Ingredient receptical) {
        if (fluid.isEmpty()) {
            return receptical;
        }

        List<ItemStack> stacks = Stream.of(receptical)
                .map(Ingredient::getMatchingStacks)
                .flatMap(Arrays::stream)
                .toList();
        if (stacks.isEmpty()) {
            stacks = Stream.of(Ingredient.fromTag(PSTags.Items.DRINK_RECEPTICALS))
                    .map(Ingredient::getMatchingStacks)
                    .flatMap(Arrays::stream)
                    .toList();
        }

        return Ingredient.ofStacks(stacks.stream()
                .map(stack -> ItemFluids.set(stack, getAsItemFluid(FluidCapacity.get(stack))))
                .toArray(ItemStack[]::new));
    }
}

