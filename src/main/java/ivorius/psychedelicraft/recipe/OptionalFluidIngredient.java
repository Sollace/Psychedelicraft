package ivorius.psychedelicraft.recipe;

import java.util.Optional;
import java.util.function.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    public static OptionalFluidIngredient fromJson(JsonObject json) {
        return new OptionalFluidIngredient(
                json.has("fluid") ? Optional.of(FluidIngredient.fromJson(json.get("fluid"))) : Optional.empty(),
                Optional.of(Ingredient.fromJson(json)).filter(i -> !i.isEmpty())
        );
    }

    public static DefaultedList<OptionalFluidIngredient> fromJsonArray(JsonArray json) {
        DefaultedList<OptionalFluidIngredient> defaultedList = DefaultedList.of();
        for (int i = 0; i < json.size(); ++i) {
            OptionalFluidIngredient ingredient = fromJson(json.get(i).getAsJsonObject());
            if (ingredient.isEmpty()) continue;
            defaultedList.add(ingredient);
        }
        return defaultedList;
    }

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

