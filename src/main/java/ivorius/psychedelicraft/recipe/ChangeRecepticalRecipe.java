package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;

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
            Identifier.CODEC.fieldOf("id").forGetter(ChangeRecepticalRecipe::getId),
            Codec.STRING.optionalFieldOf("group", "").forGetter(ChangeRecepticalRecipe::getGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ChangeRecepticalRecipe::getCategory),
            ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ChangeRecepticalRecipe::getIngredients)
    ).apply(instance, ChangeRecepticalRecipe::new));
    public static final PacketCodec<PacketByteBuf, ChangeRecepticalRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.IDENTIFIER, ChangeRecepticalRecipe::getId,
            PacketCodecs.STRING, ChangeRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ChangeRecepticalRecipe::getCategory,
            PacketCodecs.ITEM_STACK, recipe -> recipe.output,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, ChangeRecepticalRecipe::getIngredients,
            ChangeRecepticalRecipe::new
    );

    private final ItemStack output;

    public ChangeRecepticalRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(id, group, category, output, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CHANGE_RECEPTICAL;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        return RecipeUtils.recepticals(RecipeUtils.stacks(inventory)).count() == 1 && super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        return RecipeUtils.copyInputFluidToResult(getOutput(registries).copy(), RecipeUtils.stacks(inventory).toList());
    }
}
