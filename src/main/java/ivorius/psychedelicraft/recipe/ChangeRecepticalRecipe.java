package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created by lukas on 10.11.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * A shapeless recipe that preserves a drink bottle's contents between crafting.
 *
 * Used to change the container a fluid is in without losing any of its contents.
 *
 */
public class ChangeRecepticalRecipe extends ShapelessRecipe {
    public static final MapCodec<ChangeRecepticalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ChangeRecepticalRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(ChangeRecepticalRecipe::getCategory),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ChangeRecepticalRecipe::getIngredients)
    ).apply(instance, ChangeRecepticalRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ChangeRecepticalRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ChangeRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ChangeRecepticalRecipe::getCategory,
            ItemStack.PACKET_CODEC, recipe -> recipe.output,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, ChangeRecepticalRecipe::getIngredients,
            ChangeRecepticalRecipe::new
    );

    private final ItemStack output;

    public ChangeRecepticalRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(group, category, output, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CHANGE_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        return RecipeUtils.recepticals(inventory.getStacks().stream()).count() == 1 && super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return RecipeUtils.recepticals(inventory.getStacks().stream()).findFirst().map(input -> {
            // copy bottle contents to the new stack
            return ItemFluids.set(input.withItem(getResult(registries).getItem()), ItemFluids.of(input));
        }).orElseGet(getResult(registries)::copy);
    }
}
