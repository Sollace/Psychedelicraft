package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.PSTags;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
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
    public static final PacketCodec<RegistryByteBuf, OptionalFluidIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(FluidIngredient.PACKET_CODEC), OptionalFluidIngredient::fluid,
            PacketCodecs.optional(Ingredient.PACKET_CODEC), OptionalFluidIngredient::receptical,
            OptionalFluidIngredient::new
    );

    public boolean isEmpty() {
        return fluid.isEmpty() && receptical.isEmpty();
    }

    public Ingredient toVanillaIngredient() {
        return fluid.map(f -> f.toVanillaIngredient(receptical().orElse(Ingredient.fromTag(PSTags.Items.DRINK_RECEPTICALS)))).orElse(Ingredient.EMPTY);
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true)
            && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

