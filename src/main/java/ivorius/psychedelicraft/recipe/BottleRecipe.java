package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

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

    private final RawShapedRecipe raw;
    private final ItemStack result;

    public BottleRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        ItemStack output = getResult(registries).copy();
        if (output.getItem() instanceof DyeableItem dyeable) {
            RecipeUtils.stacks(inventory)
                .map(stack -> stack.getItem())
                .distinct()
                .map(COLORS::get)
                .filter(Objects::nonNull)
                .findFirst().ifPresent(color -> {
                    dyeable.setColor(output, color.getSignColor());
                });
        }
        return output;
    }

    public static class Serializer implements RecipeSerializer<BottleRecipe> {
        private static final Codec<BottleRecipe> CODEC = RecordCodecBuilder.<BottleRecipe>create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(recipe -> recipe.getGroup()),
                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.getCategory()),
                RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
                ItemStack.RECIPE_RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codecs.createStrictOptionalFieldCodec(Codec.BOOL, "show_notification", true).forGetter(recipe -> recipe.showNotification())
        ).apply(instance, BottleRecipe::new));

        @Override
        public Codec<BottleRecipe> codec() {
            return CODEC;
        }

        @Override
        public BottleRecipe read(PacketByteBuf packetByteBuf) {
            String string = packetByteBuf.readString();
            CraftingRecipeCategory craftingRecipeCategory = packetByteBuf.readEnumConstant(CraftingRecipeCategory.class);
            RawShapedRecipe rawShapedRecipe = RawShapedRecipe.readFromBuf(packetByteBuf);
            ItemStack itemStack = packetByteBuf.readItemStack();
            boolean bl = packetByteBuf.readBoolean();
            return new BottleRecipe(string, craftingRecipeCategory, rawShapedRecipe, itemStack, bl);
        }
        @Override
        public void write(PacketByteBuf packetByteBuf, BottleRecipe shapedRecipe) {
            packetByteBuf.writeString(shapedRecipe.getGroup());
            packetByteBuf.writeEnumConstant(shapedRecipe.getCategory());
            shapedRecipe.raw.writeToBuf(packetByteBuf);
            packetByteBuf.writeItemStack(shapedRecipe.result);
            packetByteBuf.writeBoolean(shapedRecipe.showNotification());
        }
    }
}
