package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final Codec<OptionalFluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(OptionalFluidIngredient::fluid),
                Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("receptical").forGetter(OptionalFluidIngredient::receptical)
            ).apply(instance, OptionalFluidIngredient::new)
    );
    public static final Codec<DefaultedList<OptionalFluidIngredient>> LIST_CODEC = CODEC.listOf().xmap(
            values -> DefaultedList.copyOf(EMPTY, values.toArray(OptionalFluidIngredient[]::new)),
            defaultedList -> new ArrayList<>(defaultedList)
    );

    public OptionalFluidIngredient(PacketByteBuf buffer) {
        this(buffer.readOptional(FluidIngredient::new), buffer.readOptional(Ingredient::fromPacket));
    }

    public boolean isEmpty() {
        return fluid.isEmpty() && receptical.isEmpty();
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeOptional(fluid, (a, b) -> b.write(a));
        buffer.writeOptional(receptical, (a, b) -> b.write(a));
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true) && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

