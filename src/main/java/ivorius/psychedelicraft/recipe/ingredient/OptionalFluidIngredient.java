package ivorius.psychedelicraft.recipe.ingredient;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements CustomIngredient, Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final MapCodec<OptionalFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
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

    @Deprecated
    @Override
    public Stream<RegistryEntry<Item>> getMatchingItems() {
        return receptical
                .map(Ingredient::getMatchingItems)
                .orElseGet(FluidIngredient::allRecepticals);
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public SlotDisplay toDisplay() {
        // Matches the vanilla logic in Ingredient.toDisplay()
        return new SlotDisplay.CompositeSlotDisplay(getMatchingItems().map(item -> {
            ItemStack stack = item.value().getDefaultStack();
            return fluid.map(i -> {
                return ItemFluids.set(stack, i.getAsItemFluid(FluidCapacity.get(stack)));
            }).orElse(stack);
        }).map(stack -> (SlotDisplay)new SlotDisplay.StackSlotDisplay(stack)).toList());
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return PSIngredients.FLUID;
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true)
            && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

