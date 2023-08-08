/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.*;

public interface PSTags {
    TagKey<Block> BARRELS = of("barrels");
    TagKey<Block> DRYING_TABLES = of("drying_tables");

    static TagKey<Block> of(String name) {
        return TagKey.of(Registry.BLOCK_KEY, Psychedelicraft.id(name));
    }

    interface Items {
        TagKey<Item> BOTTLES = of("bottles");
        TagKey<Item> BARRELS = of("barrels");
        TagKey<Item> PLACEABLE = of("placeable");
        TagKey<Item> DRINK_RECEPTICALS = of("drink_recepticals");
        TagKey<Item> SUITABLE_HOT_DRINK_RECEPTICALS = of("suitable_hot_drink_recepticals");
        TagKey<Item> SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS = of("suitable_alcoholic_drink_recepticals");
        TagKey<Item> CAN_GO_INTO_PAPER_BAG = of("can_go_into_paper_bag");
        TagKey<Item> DRUG_CROP_SEEDS = of("drug_crop_seeds");

        static TagKey<Item> of(String name) {
            return TagKey.of(Registry.ITEM_KEY, Psychedelicraft.id(name));
        }
    }

    interface Entities {
        TagKey<EntityType<?>> MULTIPLE_ENTITY_HALLUCINATIONS = of("multiple_entity_hallucinations");
        TagKey<EntityType<?>> SINGLE_ENTITY_HALLUCINATIONS = of("single_entity_hallucinations");

        static TagKey<EntityType<?>> of(String name) {
            return TagKey.of(Registry.ENTITY_TYPE_KEY, Psychedelicraft.id(name));
        }
    }

    static void bootstrap() {}
}