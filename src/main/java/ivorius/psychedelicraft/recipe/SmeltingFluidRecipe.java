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
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import com.google.gson.*;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import ivorius.psychedelicraft.util.Compat119;

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

    private WeakReference<Inventory> lastQueriedInventory = new WeakReference<>(null);

    public SmeltingFluidRecipe(
            Identifier id, String group,
            FluidIngredient fluid, Ingredient inputStack,
            Map<String, Modification> outputModifications,
            ItemStack outputStack,
            float experience, int cookingTime) {
        super(id, group, inputStack, outputStack, experience, cookingTime);
        this.fluid = fluid;
        this.outputModifications = outputModifications;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SMELTING_RECEPTICAL;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return (input.isEmpty() || input.test(inventory.getStack(0))) && fluid.test(inventory.getStack(0));
    }

    @Override
    public ItemStack getOutput() {
        Inventory inventory = lastQueriedInventory.get();
        if (inventory == null) {
            return super.getOutput();
        }
        return craft(inventory);
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        lastQueriedInventory = new WeakReference<>(inventory);
        ItemStack stack = output.isEmpty() ? Compat119.copyWithCount(inventory.getStack(0),
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

    record Modification(int value, Ops type) implements Int2IntFunction {
        enum Ops {
            SET((a, b) -> b),
            ADD((a, b) -> a + b),
            SUBTRACT((a, b) -> a - b),
            MULTIPLY((a, b) -> a * b),
            DIVIDE((a, b) -> a / b);

            private final String name;
            private final Op operation;

            static final Map<String, Ops> VALUES = Arrays.stream(values()).collect(Collectors.toMap(a -> a.name, Functions.identity()));

            Ops(Op operation) {
                this.name = name().toLowerCase(Locale.ROOT);
                this.operation = operation;
            }
        }

        Modification(PacketByteBuf buffer) {
            this(buffer.readVarInt(), buffer.readEnumConstant(Ops.class));
        }

        interface Op {
            int apply(int a, int b);
        }

        static Map<String, Modification> fromJson(JsonObject json) {
            return json.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                JsonObject obj = JsonHelper.asObject(entry.getValue(), "attribute");
                return new Modification(
                        JsonHelper.getInt(obj, "value"),
                        Ops.VALUES.getOrDefault(JsonHelper.getString(obj, "type").toLowerCase(Locale.ROOT), Ops.ADD)
                );
            }));
        }

        @Override
        public int get(int v) {
            return type.operation.apply(v, value);
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeVarInt(value);
            buffer.writeEnumConstant(type);
        }
    }



    static class Serializer implements RecipeSerializer<SmeltingFluidRecipe> {
        @Override
        public SmeltingFluidRecipe read(Identifier id, JsonObject json) {
            return new SmeltingFluidRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "input")),
                    json.has("item") ? Ingredient.fromJson(json.get("item")) : Ingredient.empty(),
                    Modification.fromJson(JsonHelper.getObject(JsonHelper.getObject(json, "result"), "attributes", new JsonObject())),
                    JsonHelper.getObject(json, "result").has("item") ? ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")) : ItemStack.EMPTY,
                    JsonHelper.getFloat(json, "experience"),
                    JsonHelper.getInt(json, "cookingTime", 200)
            );
        }

        @Override
        public SmeltingFluidRecipe read(Identifier id, PacketByteBuf buffer) {
            return new SmeltingFluidRecipe(id,
                    buffer.readString(),
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
            recipe.fluid.write(buffer);
            recipe.input.write(buffer);
            buffer.writeMap(recipe.outputModifications, PacketByteBuf::writeString, (b, c) -> c.write(b));
            buffer.writeItemStack(recipe.getOutput());
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookTime());
        }
    }
}
