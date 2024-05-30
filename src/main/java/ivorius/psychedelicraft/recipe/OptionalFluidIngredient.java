package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.PSTags;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final Codec<OptionalFluidIngredient> CODEC = CodecUtils.extend(Ingredient.ALLOW_EMPTY_CODEC, FluidIngredient.CODEC.fieldOf("fluid")).xmap(
        pair -> new OptionalFluidIngredient(pair.getSecond(), pair.getFirst()),
        ingredient -> new Pair<>(ingredient.receptical(), ingredient.fluid())
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

    public Ingredient toVanillaIngredient() {
        return fluid.map(f -> f.toVanillaIngredient(receptical().orElse(Ingredient.fromTag(PSTags.Items.DRINK_RECEPTICALS)))).orElse(Ingredient.EMPTY);
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true) && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

