package ivorius.psychedelicraft.recipe;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final Codec<OptionalFluidIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(OptionalFluidIngredient::fluid),
            Ingredient.CODEC.optionalFieldOf("receptical").forGetter(OptionalFluidIngredient::receptical)
    ).apply(i, OptionalFluidIngredient::new));
    public static final PacketCodec<RegistryByteBuf, OptionalFluidIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(FluidIngredient.PACKET_CODEC), OptionalFluidIngredient::fluid,
            PacketCodecs.optional(Ingredient.PACKET_CODEC), OptionalFluidIngredient::receptical,
            OptionalFluidIngredient::new
    );

    public static OptionalFluidIngredient of(FluidIngredient fluid) {
        return new OptionalFluidIngredient(Optional.of(fluid), Optional.empty());
    }

    public static OptionalFluidIngredient of(Ingredient receptical) {
        return new OptionalFluidIngredient(Optional.empty(), Optional.of(receptical));
    }

    public static OptionalFluidIngredient of(FluidIngredient fluid, Ingredient receptical) {
        return new OptionalFluidIngredient(Optional.of(fluid), Optional.of(receptical));
    }

    public boolean isEmpty() {
        return fluid.isEmpty() && receptical.isEmpty();
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true)
            && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

