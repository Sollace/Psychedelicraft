/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created from "RecipeFillDrink" by Sollace on 5 Jan 2023
 * Original by lukas on 21.10.14.
 * Recipe that takes as a config:
 * - Input Ingrediences (unshaped)
 * - Input Container
 * - Preconfigured fluid+level
 *
 * Outputs:
 * - Original Container filled with assigned fluid and level
 */
public class MixingRecipe extends ShapelessRecipe {
    public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MixingRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(MixingRecipe::getCategory),
            ItemFluids.CODEC.fieldOf("result").forGetter(MixingRecipe::getOutputFluid),
            Ingredient.CODEC.fieldOf("receptical").forGetter(i -> i.receptical),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(i -> i.input)
    ).apply(instance, MixingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, MixingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MixingRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MixingRecipe::getCategory,
            ItemFluids.PACKET_CODEC, MixingRecipe::getOutputFluid,
            Ingredient.PACKET_CODEC, recipe -> recipe.receptical,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), i -> i.input,
            MixingRecipe::new
    );
    private final Ingredient receptical;
    private final List<Ingredient> input;
    private final ItemFluids output;

    public MixingRecipe(
            String group,
            CraftingRecipeCategory category,
            ItemFluids output,
            Ingredient receptical,
            List<Ingredient> input) {
        super(group, category, ItemStack.EMPTY, Stream.concat(input.stream(), Stream.of(receptical)).toList());
        this.receptical = receptical;
        this.input = input;
        this.output = output;
    }

    public ItemFluids getOutputFluid() {
        return output;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RecipeSerializer getSerializer() {
        return PSRecipes.FILL_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        List<ItemStack> recepticals = getOutputRecepticals(inventory).toList();
        return recepticals.size() == 1
                && ItemFluids.of(recepticals.get(0)).isOf(Fluids.WATER)
                && FluidCapacity.getPercentage(recepticals.get(0)) >= 1
                && inventory.getRecipeMatcher().isCraftable(this, null);
    }

    private Stream<ItemStack> getOutputRecepticals(CraftingRecipeInput inventory) {
        return RecipeUtils.recepticals(inventory.getStacks()
                .stream())
                .filter(receptical)
                .filter(receptical -> input.stream().noneMatch(i -> i.test(receptical)) && ItemFluids.of(receptical).isOf(Fluids.WATER));
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return getOutputRecepticals(inventory)
                .findFirst()
                .map(receptical -> output.amount() <= 1 ? output.ofFilling(receptical.copy()) : ItemFluids.set(receptical.copy(), output.ofAmount(Math.min(output.amount(), FluidCapacity.get(receptical)))))
                .orElse(ItemStack.EMPTY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(
            new ShapelessCraftingRecipeDisplay(
                Stream.concat(input.stream(), Stream.of(receptical)).map(Ingredient::toDisplay).toList(),
                new SlotDisplay.CompositeSlotDisplay(receptical.getMatchingItems()
                        .map(i -> (SlotDisplay)new SlotDisplay.StackSlotDisplay(ItemFluids.set(i.value().getDefaultStack(), output)))
                        .toList()),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
            )
        );
    }
}
