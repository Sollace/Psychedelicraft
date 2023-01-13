/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.*;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

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
    private final Map<String, Modification> outputModifications;

    public SmeltingFluidRecipe(
            Identifier id, String group, CookingRecipeCategory category,
            FluidIngredient fluid, Ingredient inputStack,
            Map<String, Modification> outputModifications,
            ItemStack outputStack,
            float experience, int cookingTime) {
        super(id, group, category, inputStack, outputStack, experience, cookingTime);
        this.fluid = fluid;
        this.outputModifications = outputModifications;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SMELTING_FLUID;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return (input.isEmpty() || input.test(inventory.getStack(0))) && fluid.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack stack = output.isEmpty() ? inventory.getStack(0).copyWithCount(
                output.getItem() == Items.AIR ? 1 : output.getCount()
            ) : output.copy();
        NbtCompound tag = stack.getOrCreateSubNbt("fluid");
        outputModifications.forEach((key, modder) -> {
            if (!tag.contains(key, NbtElement.INT_TYPE)) {
                tag.putInt(key, 0);
            }
            tag.putInt(key, modder.applyAsInt(tag.getInt(key)));
        });
        return stack;
    }


    record Modification(int value, String type) implements Int2IntFunction {
        static final Map<String, Op> OPS = Map.of(
                "set", (a, b) -> b,
                "add", (a, b) -> a + b,
                "subtract", (a, b) -> a - b,
                "multiply", (a, b) -> a * b,
                "divide", (a, b) -> a / b
        );
        static final String[] OP_KEYS = OPS.keySet().toArray(String[]::new);

        Modification(PacketByteBuf buffer) {
            this(buffer.readVarInt(), OP_KEYS[buffer.readVarInt()]);
        }

        interface Op {
            int apply(int a, int b);
        }

        static Map<String, Modification> fromJson(JsonObject json) {
            return json.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                JsonObject obj = JsonHelper.asObject(entry.getValue(), "attribute");
                return new Modification(
                        JsonHelper.getInt(obj, "value"),
                        JsonHelper.getString(obj, "type").toLowerCase(Locale.ROOT)
                );
            }));
        }

        @Override
        public int get(int v) {
            return OPS.get(type).apply(v, value);
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeVarInt(value);
            buffer.writeVarInt(Arrays.binarySearch(OP_KEYS, type));
        }
    }



    static class Serializer implements RecipeSerializer<SmeltingFluidRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public SmeltingFluidRecipe read(Identifier id, JsonObject json) {
            return new SmeltingFluidRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CookingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CookingRecipeCategory.MISC),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "input")),
                    json.has("item") ? Ingredient.fromJson(json.get("item")) : Ingredient.empty(),
                    Modification.fromJson(JsonHelper.getObject(json, "result")),
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")),
                    JsonHelper.getFloat(json, "experience"),
                    JsonHelper.getInt(json, "cookingTime")
            );
        }

        @Override
        public SmeltingFluidRecipe read(Identifier id, PacketByteBuf buffer) {
            return new SmeltingFluidRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CookingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    Ingredient.fromPacket(buffer),
                    buffer.readMap(PacketByteBuf::readString, Modification::new),
                    buffer.readItemStack(),
                    buffer.readFloat(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buffer, SmeltingFluidRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.fluid.write(buffer);
            recipe.input.write(buffer);
            buffer.writeMap(recipe.outputModifications, PacketByteBuf::writeString, (b, c) -> c.write(b));
            buffer.writeItemStack(recipe.getOutput());
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookTime());
        }
    }
}
