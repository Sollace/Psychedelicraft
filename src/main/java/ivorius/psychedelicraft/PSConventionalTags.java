package ivorius.psychedelicraft;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface PSConventionalTags {

    interface Items {
        TagKey<Item> APPLES = of("foods/apple");
        TagKey<Item> PINEAPPLES = of("foods/pineapple");
        TagKey<Item> BANANAS = of("foods/banana");
        TagKey<Item> CORN = of("foods/corn");
        TagKey<Item> RICE = of("foods/rice");
        TagKey<Item> GRAPES = of("foods/grapes");
        TagKey<Item> HONEY = of("foods/honey");
        TagKey<Item> TOMATOES = of("foods/tomatoes");

        static TagKey<Item> of(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", name));
        }
    }
}
