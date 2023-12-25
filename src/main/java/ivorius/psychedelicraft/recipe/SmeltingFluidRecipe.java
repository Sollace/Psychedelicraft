/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by Sollace on 5 Jan 2023
 *
 * Recipe that alters a container's fluid when cooked in a furnace.
 *
 *  {
 *    "type": "psychedelicraft:smelting_fluid",
 *    "cookingtime": 200,
 *    "experience": 0.2,
 *    "input": {
 *      "fluid": "psychedelicraft:coffee"
 *    },
 *    "result": {
 *      "item": "minecraft:empty", <empty to keep as the same>
 *      "attributes": {
 *        "temperature": {
 *          "type": "add",
 *          "value": 1
 *        }
 *      }
 *    }
 *  }
 *
 */
public class SmeltingFluidRecipe extends SmeltingRecipe {
    private final FluidIngredient fluid;
    private final FluidModifyingResult result;

    private WeakReference<Inventory> lastQueriedInventory = new WeakReference<>(null);

    public SmeltingFluidRecipe(
            String group, CookingRecipeCategory category,
            FluidIngredient fluid, Ingredient inputStack,
            FluidModifyingResult result,
            float experience, int cookingTime) {
        super(group, category, inputStack, result.result(), experience, cookingTime);
        this.fluid = fluid;
        this.result = result;
    }

    public FluidIngredient getFluid() {
        return fluid;
    }

    public FluidModifyingResult getResult() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SMELTING_RECEPTICAL;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return (ingredient.isEmpty() || ingredient.test(inventory.getStack(0))) && fluid.test(inventory.getStack(0));
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registries) {
        Inventory inventory = lastQueriedInventory.get();
        if (inventory == null) {
            return super.getResult(registries);
        }
        return craft(inventory, registries);
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registries) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return result.applyTo(inventory.getStack(0));
    }

    static class Serializer implements RecipeSerializer<SmeltingFluidRecipe> {
        private static final Codec<SmeltingFluidRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(SmeltingFluidRecipe::getGroup),
                CookingRecipeCategory.CODEC.fieldOf("category").orElse(CookingRecipeCategory.MISC).forGetter(SmeltingFluidRecipe::getCategory),
                FluidIngredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.fluid),
                Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("item", Ingredient.empty()).forGetter(recipe -> recipe.ingredient),
                FluidModifyingResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.FLOAT.fieldOf("experience").forGetter(SmeltingFluidRecipe::getExperience),
                Codec.INT.optionalFieldOf("cookingTIme", 200).forGetter(SmeltingFluidRecipe::getCookingTime)
            ).apply(instance, SmeltingFluidRecipe::new));

        @Override
        public Codec<SmeltingFluidRecipe> codec() {
            return CODEC;
        }

        @Override
        public SmeltingFluidRecipe read(PacketByteBuf buffer) {
            return new SmeltingFluidRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CookingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    Ingredient.fromPacket(buffer),
                    new FluidModifyingResult(buffer),
                    buffer.readFloat(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buffer, SmeltingFluidRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.fluid.write(buffer);
            recipe.ingredient.write(buffer);
            recipe.result.write(buffer);
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}
