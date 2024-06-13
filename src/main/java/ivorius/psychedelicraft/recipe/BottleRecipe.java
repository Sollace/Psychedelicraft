package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;

public class BottleRecipe extends ShapedRecipe {
    public static final Map<Item, DyeColor> COLORS = Util.make(new HashMap<>(), map -> {
        map.put(Items.WHITE_STAINED_GLASS, DyeColor.WHITE);
        map.put(Items.ORANGE_STAINED_GLASS, DyeColor.ORANGE);
        map.put(Items.MAGENTA_STAINED_GLASS, DyeColor.MAGENTA);
        map.put(Items.LIGHT_BLUE_STAINED_GLASS, DyeColor.LIGHT_BLUE);
        map.put(Items.YELLOW_STAINED_GLASS, DyeColor.YELLOW);
        map.put(Items.LIME_STAINED_GLASS, DyeColor.LIME);
        map.put(Items.PINK_STAINED_GLASS, DyeColor.PINK);
        map.put(Items.GRAY_STAINED_GLASS, DyeColor.GRAY);
        map.put(Items.LIGHT_GRAY_STAINED_GLASS, DyeColor.LIGHT_GRAY);
        map.put(Items.CYAN_STAINED_GLASS, DyeColor.CYAN);
        map.put(Items.PURPLE_STAINED_GLASS, DyeColor.PURPLE);
        map.put(Items.BLUE_STAINED_GLASS, DyeColor.BLUE);
        map.put(Items.BROWN_STAINED_GLASS, DyeColor.BROWN);
        map.put(Items.GREEN_STAINED_GLASS, DyeColor.GREEN);
        map.put(Items.RED_STAINED_GLASS, DyeColor.RED);
        map.put(Items.BLACK_STAINED_GLASS, DyeColor.BLACK);
    });
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
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        ItemStack output = getResult(registries).copy();
        inventory.getStacks().stream()
            .map(stack -> stack.getItem())
            .distinct()
            .map(COLORS::get)
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(color -> output.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color.getSignColor(), true)));
        return output;
    }
}
