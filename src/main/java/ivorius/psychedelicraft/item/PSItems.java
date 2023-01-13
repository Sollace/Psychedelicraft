/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import static ivorius.psychedelicraft.fluid.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.entity.drugs.*;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSItems {
    DrinkableItem WOODEN_MUG = register("wooden_mug", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 2, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem STONE_CUP = register("stone_cup", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 20, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem GLASS_CHALICE = register("glass_chalice", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 5, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));

    FlaskItem OAK_BARREL = register("oak_barrel", new FlaskItem(PSBlocks.OAK_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    FlaskItem SPRUCE_BARREL = register("spruce_barrel", new FlaskItem(PSBlocks.SPRUCE_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    FlaskItem BIRCH_BARREL = register("birch_barrel", new FlaskItem(PSBlocks.BIRCH_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    FlaskItem JUNGLE_BARREL = register("jungle_barrel", new FlaskItem(PSBlocks.JUNGLE_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    FlaskItem ACACIA_BARREL = register("acacia_barrel", new FlaskItem(PSBlocks.ACACIA_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    FlaskItem DARK_OAK_BARREL = register("dark_oak_barrel", new FlaskItem(PSBlocks.DARK_OAK_BARREL, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));

    FlaskItem MASH_TUB = register("mash_tub", new MashTubItem(PSBlocks.MASH_TUB, new Settings().maxCount(16)));
    FlaskItem FLASK = register("flask", new FlaskItem(PSBlocks.FLASK, new Settings().maxCount(16), FlaskBlockEntity.FLASK_CAPACITY));
    FlaskItem DISTILLERY = register("distillery", new FlaskItem(PSBlocks.DISTILLERY, new Settings().maxCount(16), DistilleryBlockEntity.DISTILLERY_CAPACITY));
    RiftJarItem RIFT_JAR = register("rift_jar", new RiftJarItem(PSBlocks.RIFT_JAR, new Settings()));

    Item WINE_GRAPES = register("wine_grapes", new WineGrapesItem(new Settings().food(
            new FoodComponent.Builder().hunger(1).saturationModifier(0.5F).meat().build()
    ), 15));
    DrinkableItem BOTTLE = register("bottle", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER * 2, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    MolotovCocktailItem MOLOTOV_COCKTAIL = register("molotov_cocktail", new MolotovCocktailItem(new Settings().maxCount(16), MILLIBUCKETS_PER_LITER * 2));

    Item CANNABIS_SEEDS = register("cannabis_seeds", new AliasedBlockItem(PSBlocks.CANNABIS, new Settings()));
    Item CANNABIS_LEAF = register("cannabis_leaf");
    Item CANNABIS_BUDS = register("cannabis_buds");
    Item DRIED_CANNABIS_LEAF = register("dried_cannabis_leaf");
    Item DRIED_CANNABIS_BUDS = register("dried_cannabis_buds");

    Item HASH_MUFFIN = register("hash_muffin", new EdibleItem(
            new Settings().food(EdibleItem.HAS_MUFFIN),
            new DrugInfluence(DrugType.CANNABIS, 60, 0.004, 0.002, 0.7f)
    ));

    Item HOP_CONES = register("hop_cones");
    Item HOP_SEEDS = register("hop_seeds", new AliasedBlockItem(PSBlocks.HOP, new Settings()));

    Item BROWN_MAGIC_MUSHROOMS = register("brown_magic_mushrooms", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.BROWN_SHROOMS, 15, 0.005, 0.003, 0.5f)
    ));
    Item RED_MAGIC_MUSHROOMS = register("red_magic_mushrooms", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.RED_SHROOMS, 15, 0.005, 0.003, 0.5f)
    ));

    Item TOBACCO_LEAVES = register("tobacco");
    Item TOBACCO_SEEDS = register("tobacco_seeds", new AliasedBlockItem(PSBlocks.TOBACCO, new Settings()));
    Item DRIED_TOBACCO = register("dried_tobacco");

    SmokeableItem CIGARETTE = register("cigarette", new SmokeableItem(
            new Settings().maxCount(1).maxDamage(1), 2,
            new DrugInfluence(DrugType.TOBACCO, 0, 0.1, 0.02, 0.7f)
    ));
    SmokeableItem CIGAR = register("cigar", new SmokeableItem(
            new Settings().maxCount(1).maxDamage(3), 4,
            new DrugInfluence(DrugType.TOBACCO, 0, 0.1, 0.02, 0.7f)
    ));
    SmokeableItem JOINT = register("joint", new SmokeableItem(
            new Settings().maxCount(1).maxDamage(2), 2,
            new DrugInfluence(DrugType.CANNABIS, 20, 0.002, 0.001, 0.20f)
    ));

    Item COCA_SEEDS = register("coca_seeds", new AliasedBlockItem(PSBlocks.COCA, new Settings()));
    Item COCA_LEAVES = register("coca_leaves");
    Item DRIED_COCA_LEAVES = register("dried_coca_leaves");
    Item COCAINE_POWDER = register("cocaine_powder", new CocainePowderItem(new Settings(), new DrugInfluence(DrugType.COCAINE, 0, 0.002, 0.003, 0.35f)));

    InjectableItem SYRINGE = register("syringe", new InjectableItem(new Settings(), MILLIBUCKETS_PER_LITER / 100));

    Item JUNIPER_LEAVES = register("juniper_leaves", PSBlocks.JUNIPER_LEAVES);
    Item FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", PSBlocks.FRUITING_JUNIPER_LEAVES);
    Item JUNIPER_LOG = register("juniper_log", PSBlocks.JUNIPER_LOG);
    Item JUNIPER_WOOD = register("juniper_wood", PSBlocks.JUNIPER_WOOD);
    Item STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", PSBlocks.STRIPPED_JUNIPER_LOG);
    Item STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", PSBlocks.STRIPPED_JUNIPER_WOOD);
    Item JUNIPER_BERRIES = register("juniper_berries", new SpecialFoodItem(
            new Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.5F).meat().build()), 15
    ));
    Item JUNIPER_SAPLING = register("juniper_sapling", PSBlocks.JUNIPER_SAPLING);

    Item COFFEA_CHERRIES = register("coffea_cherries", new AliasedBlockItem(PSBlocks.COFFEA, new Settings()));
    Item COFFEE_BEANS = register("coffee_beans");

    Item PEYOTE = register("peyote", PSBlocks.PEYOTE);
    Item DRIED_PEYOTE = register("dried_peyote", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.PEYOTE, 15, 0.005, 0.003, 0.5f)
    ));
    Item PEYOTE_JOINT = register("peyote_joint", new SmokeableItem(
            new Settings().maxCount(1).maxDamage(2), 2,
            new DrugInfluence(DrugType.PEYOTE, 20, 0.003, 0.0015, 0.4f),
            new DrugInfluence(DrugType.TOBACCO, 0, 0.1, 0.02, 0.1f)
    ));

    Item LATTICE = register("lattice", PSBlocks.LATTICE);
    Item BOTTLE_RACK = register("bottle_rack", PSBlocks.BOTTLE_RACK);
    Item DRYING_TABLE = register("drying_table", PSBlocks.DRYING_TABLE);
    Item IRON_DRYING_TABLE = register("iron_drying_table", PSBlocks.IRON_DRYING_TABLE);

    HarmoniumItem HARMONIUM = register("harmonium", new HarmoniumItem(new Settings()));

    BongItem SMOKING_PIPE = register("smoking_pipe", new BongItem(new Settings().maxDamage(50)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultStack(), new DrugInfluence(DrugType.CANNABIS, 20, 0.002, 0.001, 0.25F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultStack(), new DrugInfluence(DrugType.TOBACCO, 0, 0.1, 0.02, 0.8f)))
            .consumes(new BongItem.Consumable(HARMONIUM.getDefaultStack(), stack -> new DrugInfluenceHarmonium(0, 0.04, 0.01, 0.65f, MathUtils.unpackRgb(HARMONIUM.getColor(stack)))));
    // TODO: Play around with the bongs benefits
    BongItem BONG = register("bong", new BongItem(new Settings().maxDamage(128)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultStack(), new DrugInfluence(DrugType.CANNABIS, 20, 0.002, 0.001, 0.2F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultStack(), new DrugInfluence(DrugType.TOBACCO, 0, 0.1, 0.02, 0.6F)));

    static Item register(String name, Block block) {
        return register(name, new BlockItem(block, new Item.Settings()));
    }

    static Item register(String name) {
        return register(name, new Item(new Item.Settings()));
    }

    static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Psychedelicraft.id(name), item);
    }

    static void bootstrap() { }
}
