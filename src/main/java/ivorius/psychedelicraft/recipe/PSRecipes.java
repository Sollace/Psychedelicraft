package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<FillRecepticalRecipe> FILL_RECEPTICAL = RecipeSerializer.register("psychedelicraft:fill_receptical", new Serializer<>(FillRecepticalRecipe.CODEC, FillRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<ChangeRecepticalRecipe> CHANGE_RECEPTICAL = RecipeSerializer.register("psychedelicraft:change_receptical", new Serializer<>(ChangeRecepticalRecipe.CODEC, ChangeRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<PouringRecipe> POUR_DRINK = RecipeSerializer.register("psychedelicraft:pour_drink", new SpecialRecipeSerializer<>(PouringRecipe::new));
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_RECEPTICAL = RecipeSerializer.register("psychedelicraft:smelting_receptical", new Serializer<>(SmeltingFluidRecipe.CODEC, SmeltingFluidRecipe.PACKET_CODEC));
    RecipeSerializer<BottleRecipe> CRAFTING_SHAPED = RecipeSerializer.register("psychedelicraft:crafting_shaped", new Serializer<>(BottleRecipe.CODEC, BottleRecipe.PACKET_CODEC));
    RecipeSerializer<FluidAwareShapelessRecipe> SHAPELESS_FLUID = RecipeSerializer.register("psychedelicraft:shapeless_fluid", new Serializer<>(FluidAwareShapelessRecipe.CODEC, FluidAwareShapelessRecipe.PACKET_CODEC));

    RecipeType<MashingRecipe> MASHING_TYPE = RecipeType.register("psychedelicraft:mashing");
    RecipeSerializer<MashingRecipe> MASHING = RecipeSerializer.register("psychedelicraft:mashing", new MashingRecipe.Serializer());

    RecipeType<DryingRecipe> DRYING_TYPE = RecipeType.register("psychedelicraft:drying");
    RecipeSerializer<DryingRecipe> DRYING = RecipeSerializer.register("psychedelicraft:drying", new Serializer<>(DryingRecipe.CODEC, DryingRecipe.PACKET_CODEC));

    static void bootstrap() {
        LootTableEvents.MODIFY.register((key, supplier, source) -> {
            Identifier id = key.getValue();
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }
            // TODO: Check if table exists?
            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.dungeonChests())) {
                supplier.pool(LootPool.builder().with(LootTableEntry.builder(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("psychedelicraftmc", id.getPath())))));
            }
        });
    }


    record Serializer<T extends Recipe<?>> (
            MapCodec<T> codec,
            PacketCodec<RegistryByteBuf, T> packetCodec
        ) implements RecipeSerializer<T> {
    }
}
