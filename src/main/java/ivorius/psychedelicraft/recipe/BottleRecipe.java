package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
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

    public BottleRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResult(null));
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

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public Codec<ShapedRecipe> codec() {
            return super.codec().xmap(BottleRecipe::new, Function.identity());
        }

        @Override
        public ShapedRecipe read(PacketByteBuf buffer) {
            return new BottleRecipe(super.read(buffer));
        }
    }
}
