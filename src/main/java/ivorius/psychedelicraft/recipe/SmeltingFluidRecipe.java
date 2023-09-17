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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.base.Functions;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

    private WeakReference<Inventory> lastQueriedInventory = new WeakReference<>(null);

    public SmeltingFluidRecipe(
            String group, CookingRecipeCategory category,
            FluidIngredient fluid, Ingredient inputStack,
            Map<String, Modification> outputModifications,
            ItemStack outputStack,
            float experience, int cookingTime) {
        super(group, category, inputStack, outputStack, experience, cookingTime);
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
        ItemStack stack = result.isEmpty() ? inventory.getStack(0).copyWithCount(
                result.getItem() == Items.AIR ? 1 : result.getCount()
            ) : result.copy();
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

            private static final Map<String, Ops> VALUES = Arrays.stream(values()).collect(Collectors.toMap(a -> a.name, Functions.identity()));

            Ops(Op operation) {
                this.name = name().toLowerCase(Locale.ROOT);
                this.operation = operation;
            }
        }

        Modification(PacketByteBuf buffer) {
            this(buffer.readVarInt(), buffer.readEnumConstant(Ops.class));
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

        interface Op {
            int apply(int a, int b);
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
        @SuppressWarnings("deprecation")
        private static final Codec<Map<String, Modification>> ATTRIBUTES_CODEC = Codecs.fromJsonSerializer(
                json -> Modification.fromJson(JsonHelper.getObject(json.getAsJsonObject(), "attributes", new JsonObject())),
                data -> {
                    throw new NotImplementedException("Cannot serialize a modifications map!");
                }
        );

        private static final Codec<SmeltingFluidRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(SmeltingFluidRecipe::getGroup),
                CookingRecipeCategory.CODEC.fieldOf("category").orElse(CookingRecipeCategory.MISC).forGetter(SmeltingFluidRecipe::getCategory),
                FluidIngredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.fluid),
                Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("item", Ingredient.empty()).forGetter(recipe -> recipe.ingredient),
                ATTRIBUTES_CODEC.fieldOf("result").forGetter(recipe -> recipe.outputModifications),
                RecipeUtils.ITEM_STACK_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(recipe -> recipe.result),
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
            recipe.ingredient.write(buffer);
            buffer.writeMap(recipe.outputModifications, PacketByteBuf::writeString, (b, c) -> c.write(b));
            buffer.writeItemStack(recipe.result);
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}
