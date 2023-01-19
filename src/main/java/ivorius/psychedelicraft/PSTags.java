/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;

public interface PSTags {
    TagKey<Block> BARRELS = of("barrels");
    TagKey<Block> DRYING_TABLES = of("drying_tables");

    static TagKey<Block> of(String name) {
        return TagKey.of(RegistryKeys.BLOCK, Psychedelicraft.id(name));
    }

    interface Items {
        TagKey<Item> BOTTLES = of("bottles");
        TagKey<Item> BARRELS = of("barrels");

        static TagKey<Item> of(String name) {
            return TagKey.of(RegistryKeys.ITEM, Psychedelicraft.id(name));
        }
    }

    static void bootstrap() {}
}