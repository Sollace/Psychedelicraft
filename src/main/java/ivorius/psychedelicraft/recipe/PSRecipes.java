package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.recipe.ingredient.PSIngredients;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<MixingRecipe> FILL_RECEPTICAL = serializer("fill_receptical", new Serializer<>(MixingRecipe.CODEC, MixingRecipe.PACKET_CODEC));
    RecipeSerializer<ChangeRecepticalRecipe> CHANGE_RECEPTICAL = serializer("change_receptical", new Serializer<>(ChangeRecepticalRecipe.CODEC, ChangeRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<PouringRecipe> CRAFTING_POURING = serializer("crafting_pouring", new SpecialRecipeSerializer<>(PouringRecipe::new));
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_RECEPTICAL = serializer("smelting_receptical", new Serializer<>(SmeltingFluidRecipe.CODEC, SmeltingFluidRecipe.PACKET_CODEC));
    @SuppressWarnings({ "unchecked", "rawtypes" })
    RecipeSerializer<BottleRecipe> CRAFTING_SHAPED = serializer("crafting_shaped", (RecipeSerializer)new BottleRecipe.Serializer());
    RecipeSerializer<FluidAwareShapelessRecipe> CRAFTING_SHAPELESS_FLUID = serializer("crafting_shapeless_fluid", new Serializer<>(FluidAwareShapelessRecipe.CODEC, FluidAwareShapelessRecipe.PACKET_CODEC));

    RecipeType<MashingRecipe> MASHING_TYPE = type("mashing");
    RecipeSerializer<MashingRecipe> MASHING = serializer("mashing", new Serializer<>(MashingRecipe.CODEC, MashingRecipe.PACKET_CODEC));

    RecipeType<BunsenBurnerRecipe> CHEMISTRY = type("chemistry");
    RecipeSerializer<ReactingRecipe> REACTING = serializer("reacting", new Serializer<>(ReactingRecipe.CODEC, ReactingRecipe.PACKET_CODEC));

    RecipeType<DryingRecipe> DRYING_TYPE = type("drying");
    RecipeSerializer<DryingRecipe> DRYING = serializer("drying", new Serializer<>(DryingRecipe.CODEC, DryingRecipe.PACKET_CODEC));

    RecipeType<HardeningRecipe> TRAY = type("tray");
    RecipeSerializer<HardeningRecipe> HARDENING = serializer("hardening", new Serializer<>(HardeningRecipe.CODEC, HardeningRecipe.PACKET_CODEC));

    static <T extends Recipe<?>> RecipeType<T> type(String name) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S serializer(String name, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Psychedelicraft.id(name), serializer);
    }

    static void bootstrap() {
        PSIngredients.bootstrap();
    }

    record Serializer<T extends Recipe<?>> (
            Codec<T> codec,
            PacketCodec<PacketByteBuf, T> packetCodec
        ) implements RecipeSerializer<T> {
        Serializer(
                MapCodec<T> codec,
                PacketCodec<PacketByteBuf, T> packetCodec
            ) {
            this(codec.codec(), packetCodec);
        }

        @Override
        public T read(PacketByteBuf buf) {
            return packetCodec.decode(buf);
        }

        @Override
        public void write(PacketByteBuf buf, T recipe) {
            packetCodec.encode(buf, recipe);
        }
    }
}
