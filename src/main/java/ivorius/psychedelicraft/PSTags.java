/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;

public interface PSTags {
    interface Items {
        TagKey<Item> BOTTLE_RACK_INSERTABLE = of("bottle_rack_insertable");
        TagKey<Item> BARRELS = of("barrels");
        TagKey<Item> JUNIPER_LOGS = of("juniper_logs");
        TagKey<Item> DRYING_TABLES = of("drying_tables");
        TagKey<Item> PLACEABLE_RECEPTICALS = of("receptical/placeable");
        TagKey<Item> DRINK_RECEPTICALS = of("receptical/drinks");
        TagKey<Item> DRUG_RECEPTICALS = of("receptical/drugs");
        TagKey<Item> SUITABLE_HOT_DRINK_RECEPTICALS = of("receptical/suitable_for_hot_drinks");
        TagKey<Item> SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS = of("receptical/suitable_for_alcoholic_drinks");
        TagKey<Item> CAN_GO_INTO_PAPER_BAG = of("can_go_into_paper_bag");
        TagKey<Item> DRUG_CROP_SEEDS = of("drug_crop_seeds");

        TagKey<Item> MORNING_GLORY_INGREDIENTS = of("ingredients/morning_glory");

        static TagKey<Item> of(String name) {
            return TagKey.of(RegistryKeys.ITEM, Psychedelicraft.id(name));
        }
    }

    interface Entities {
        TagKey<EntityType<?>> MULTIPLE_ENTITY_HALLUCINATIONS = of("multiple_entity_hallucinations");
        TagKey<EntityType<?>> SINGLE_ENTITY_HALLUCINATIONS = of("single_entity_hallucinations");

        static TagKey<EntityType<?>> of(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Psychedelicraft.id(name));
        }
    }

    interface Blocks {
        TagKey<Block> BARRELS = of("barrels");
        TagKey<Block> LATTICES = of("lattices");
        TagKey<Block> DRYING_TABLES = of("drying_tables");

        TagKey<Block> JUNIPER_LOGS = of("juniper_logs");
        TagKey<Block> NIGHTSHADE = of("nightshade");

        static TagKey<Block> of(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Psychedelicraft.id(name));
        }
    }

    static void bootstrap() {}
}