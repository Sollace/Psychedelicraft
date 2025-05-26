package ivorius.psychedelicraft.recipe.ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.compat.IngredientCompat;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements CustomIngredient, Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final MapCodec<OptionalFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(OptionalFluidIngredient::fluid),
            IngredientCompat.DISALLOW_EMPTY_CODEC.optionalFieldOf("receptical").forGetter(OptionalFluidIngredient::receptical)
    ).apply(i, OptionalFluidIngredient::new));
    public static final Codec<DefaultedList<OptionalFluidIngredient>> LIST_CODEC = CODEC.codec().listOf().xmap(
            values -> DefaultedList.copyOf(EMPTY, values.toArray(OptionalFluidIngredient[]::new)),
            defaultedList -> new ArrayList<>(defaultedList)
    );
    public static final PacketCodec<PacketByteBuf, OptionalFluidIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(FluidIngredient.PACKET_CODEC), OptionalFluidIngredient::fluid,
            PacketCodecs.optional(PacketCodecs.INGREDIENT), OptionalFluidIngredient::receptical,
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
    public List<ItemStack> getMatchingStacks() {
        return receptical
                .map(ingredient -> Arrays.stream(ingredient.getMatchingStacks()))
                .orElseGet(FluidIngredient::allRecepticals)
                .map(fluid
                        .map(fluid -> (Function<ItemStack, ItemStack>)(receptical -> ItemFluids.set(receptical, fluid.getAsItemFluid(FluidCapacity.get(receptical)))))
                        .orElse(Function.identity()))
                .toList();
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return PSIngredients.OPTIONAL_FLUID;
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true)
            && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

