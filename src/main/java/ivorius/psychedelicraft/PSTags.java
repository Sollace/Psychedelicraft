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

    static TagKey<Block> of(String name) {
        return TagKey.of(RegistryKeys.BLOCK, Psychedelicraft.id(name));
    }

    interface Items {
        TagKey<Item> BOTTLES = of("bottles");

        static TagKey<Item> of(String name) {
            return TagKey.of(RegistryKeys.ITEM, Psychedelicraft.id(name));
        }
    }

    static void bootstrap() {}
}