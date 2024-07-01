package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Stainable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Colors;

public class BottleRecipe extends ShapedRecipe {
    public static final MapCodec<BottleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(BottleRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(BottleRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BottleRecipe::showNotification)
    ).apply(instance, BottleRecipe::new));
    public static final PacketCodec<RegistryByteBuf, BottleRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, BottleRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, BottleRecipe::getCategory,
            RawShapedRecipe.PACKET_CODEC, recipe -> recipe.raw,
            ItemStack.PACKET_CODEC, recipe -> recipe.result,
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
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        ItemStack output = RecipeUtils.copyInputFluidToResult(getResult(registries).copy(), inventory.getStacks());
        inventory.getStacks().stream().mapToInt(stack -> {
                if (stack.getItem() instanceof BlockItem i && i.getBlock() instanceof Stainable s) {
                    return s.getColor().getSignColor();
                }
                return DyedColorComponent.getColor(stack, Colors.WHITE);
            })
            .filter(color -> color != Colors.WHITE)
            .findFirst()
            .ifPresent(color -> output.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, true)));
        return output;
    }
}
