package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ChangeRecepticalRecipe::getCategory),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(instance, ChangeRecepticalRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ChangeRecepticalRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ChangeRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ChangeRecepticalRecipe::getCategory,
            ItemStack.PACKET_CODEC, recipe -> recipe.output,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), recipe -> recipe.ingredients,
            ChangeRecepticalRecipe::new
    );

    private final ItemStack output;
    private final List<Ingredient> ingredients;

    public ChangeRecepticalRecipe(String group, CraftingRecipeCategory category, ItemStack output, List<Ingredient> input) {
        super(group, category, output, input);
        this.ingredients = input;
        this.output = output;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RecipeSerializer getSerializer() {
        return PSRecipes.CHANGE_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        return RecipeUtils.recepticals(inventory.getStacks().stream()).count() == 1 && super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, WrapperLookup registries) {
        return RecipeUtils.copyInputFluidToResult(super.craft(input, registries), input.getStacks());
    }
}
