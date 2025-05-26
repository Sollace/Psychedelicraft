package ivorius.psychedelicraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.block.Stainable;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class BottleRecipe extends ShapedRecipe {
    public BottleRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput(null), recipe.showNotification());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_SHAPED;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        ItemStack output = RecipeUtils.copyInputFluidToResult(getOutput(registries).copy(), RecipeUtils.stacks(inventory).toList());
        RecipeUtils.stacks(inventory).mapToInt(stack -> {
                if (stack.getItem() instanceof BlockItem i && i.getBlock() instanceof Stainable s) {
                    return s.getColor().getSignColor();
                }
                if (stack.getItem() instanceof DyeableItem dyeable) {
                    return dyeable.getColor(stack);
                }
                return Colors.WHITE;
            })
            .filter(color -> color != Colors.WHITE)
            .findFirst()
            .ifPresent(color -> {
                if (output.getItem() instanceof DyeableItem dyeable) {
                    dyeable.setColor(output, color);
                }
            });
        return output;
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe read(Identifier id, JsonObject json) {
            return new BottleRecipe(super.read(id, json));
        }

        @Override
        public ShapedRecipe read(Identifier id, PacketByteBuf buffer) {
            return new BottleRecipe(super.read(id, buffer));
        }
    }
}
