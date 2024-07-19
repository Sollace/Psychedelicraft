package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<FillRecepticalRecipe> FILL_RECEPTICAL = serializer("fill_receptical", new Serializer<>(FillRecepticalRecipe.CODEC, FillRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<ChangeRecepticalRecipe> CHANGE_RECEPTICAL = serializer("change_receptical", new Serializer<>(ChangeRecepticalRecipe.CODEC, ChangeRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<PouringRecipe> POUR_DRINK = serializer("pour_drink", new SpecialRecipeSerializer<>(PouringRecipe::new));
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_RECEPTICAL = serializer("smelting_receptical", new Serializer<>(SmeltingFluidRecipe.CODEC, SmeltingFluidRecipe.PACKET_CODEC));
    RecipeSerializer<BottleRecipe> CRAFTING_SHAPED = serializer("crafting_shaped", new Serializer<>(BottleRecipe.CODEC, BottleRecipe.PACKET_CODEC));
    RecipeSerializer<FluidAwareShapelessRecipe> SHAPELESS_FLUID = serializer("shapeless_fluid", new Serializer<>(FluidAwareShapelessRecipe.CODEC, FluidAwareShapelessRecipe.PACKET_CODEC));

    RecipeType<MashingRecipe> MASHING_TYPE = type("mashing");
    RecipeSerializer<MashingRecipe> MASHING = serializer("mashing", new Serializer<>(MashingRecipe.CODEC, MashingRecipe.PACKET_CODEC));

    RecipeType<ReducingRecipe> REACTING_TYPE = type("reacting");
    RecipeSerializer<ReducingRecipe> REDUCING = serializer("reducing", new Serializer<>(ReducingRecipe.CODEC, ReducingRecipe.PACKET_CODEC));

    RecipeType<DryingRecipe> DRYING_TYPE = type("drying");
    RecipeSerializer<DryingRecipe> DRYING = serializer("drying", new Serializer<>(DryingRecipe.CODEC, DryingRecipe.PACKET_CODEC));

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

    static void bootstrap() { }

    record Serializer<T extends Recipe<?>> (
            MapCodec<T> codec,
            PacketCodec<RegistryByteBuf, T> packetCodec
        ) implements RecipeSerializer<T> {
    }
}
