package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.minecraft.block.Stainable;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Colors;

public class BottleRecipe extends ShapedRecipe {
    public static final MapCodec<BottleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(BottleRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(BottleRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
            ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BottleRecipe::showNotification)
    ).apply(instance, BottleRecipe::new));
    public static final PacketCodec<PacketByteBuf, BottleRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, BottleRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, BottleRecipe::getCategory,
            PacketCodecs.RAW_SHAPED_RECIPE, recipe -> recipe.raw,
            PacketCodecs.ITEM_STACK, recipe -> recipe.result,
            PacketCodecs.BOOL, BottleRecipe::showNotification,
            BottleRecipe::new
    );

    private final RawShapedRecipe raw;
    private final ItemStack result;

    public BottleRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_SHAPED;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        ItemStack output = RecipeUtils.copyInputFluidToResult(getResult(registries).copy(), RecipeUtils.stacks(inventory).toList());
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
}
