/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import static ivorius.psychedelicraft.fluids.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.blocks.*;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.entities.drugs.DrugInfluenceHarmonium;
import ivorius.psychedelicraft.fluids.ConsumableFluid;
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
public class PSItems {
    public static DrinkableItem woodenMug = register("wooden_mug", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 2, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    public static DrinkableItem stoneCup = register("stone_cup", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 20, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    public static DrinkableItem glassChalice = register("glass_chalice", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER / 5, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));

    public static FlaskItem itemBarrel = register("oak_barrel", new FlaskItem(PSBlocks.oak_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    public static FlaskItem itemSpruceBarrel = register("spruce_barrel", new FlaskItem(PSBlocks.spruce_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    public static FlaskItem itemBirchBarrel = register("birch_barrel", new FlaskItem(PSBlocks.birch_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    public static FlaskItem itemJungleBarrel = register("jungle_barrel", new FlaskItem(PSBlocks.jungle_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    public static FlaskItem itemAcaciaBarrel = register("acacia_barrel", new FlaskItem(PSBlocks.acacia_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));
    public static FlaskItem itemDarkOakBarrel = register("dark_oak_barrel", new FlaskItem(PSBlocks.dark_oak_barrel, new Settings().maxCount(16), BarrelBlockEntity.BARREL_CAPACITY));

    public static FlaskItem itemMashTub = register("mash_rub", new ItemMashTub(PSBlocks.mashTub, new Settings().maxCount(16)));
    public static FlaskItem itemFlask = register("flask", new FlaskItem(PSBlocks.flask, new Settings().maxCount(16), FlaskBlockEntity.FLASK_CAPACITY));
    public static FlaskItem itemDistillery = register("distillery", new FlaskItem(PSBlocks.distillery, new Settings().maxCount(16), DistilleryBlockEntity.DISTILLERY_CAPACITY));
    public static ItemRiftJar itemRiftJar = register("rift_jar", new ItemRiftJar(PSBlocks.riftJar, new Settings()));

    public static Item wineGrapes = register("wine_grapes", new ItemWineGrapes(new Settings().food(
            new FoodComponent.Builder().hunger(1).saturationModifier(0.5F).meat().build()
    ), 15));
    public static DrinkableItem bottle = register("bottle", new DrinkableItem(new Settings(), MILLIBUCKETS_PER_LITER * 2, DrinkableItem.FLUID_PER_DRINKING, ConsumableFluid.ConsumptionType.DRINK));
    public static ItemMolotovCocktail molotovCocktail = register("molotov_cocktail", new ItemMolotovCocktail(new Settings().maxCount(16), MILLIBUCKETS_PER_LITER * 2));

    public static Item cannabisSeeds = register("cannabis_seeds", new AliasedBlockItem(PSBlocks.cannabisPlant, new Settings()));
    public static Item cannabisLeaf = register("cannabis_leaf");
    public static Item cannabisBuds = register("cannabis_buds");
    public static Item driedCannabisBuds = register("dried_cannabis_buds");
    public static Item driedCannabisLeaves = register("dried_cannabis_leaves");

    public static Item hashMuffin = register("hash_muffin", new EdibleItem(
            new Settings().food(EdibleItem.HAS_MUFFIN),
            new DrugInfluence("Cannabis", 60, 0.004, 0.002, 0.7f)
    ));

    public static Item hopCones = register("hop_cones");
    public static Item hopSeeds = register("hop_seeds", new AliasedBlockItem(PSBlocks.hopPlant, new Settings()));

    public static Item magicMushroomsBrown = register("brown_magic_mushroom", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence("BrownShrooms", 15, 0.005, 0.003, 0.5f)
    ));
    public static Item magicMushroomsRed = register("red_magic_mushroom", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence("RedShrooms", 15, 0.005, 0.003, 0.5f)
    ));

    public static Item tobaccoLeaf = register("tobacco_leaf");
    public static Item tobaccoSeeds = register("tobacco_seeds", new AliasedBlockItem(PSBlocks.tobaccoPlant, new Settings()));
    public static Item driedTobacco = register("dried_tobacco");
    public static SmokeableItem cigarette = register("cigarette", new SmokeableItem(
            new Settings().maxCount(1), 2,
            new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.7f)
    ));
    public static SmokeableItem cigar = register("cigar", new SmokeableItem(
            new Settings().maxCount(1), 4,
            new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.7f)
    ));
    public static SmokeableItem joint = register("join", new SmokeableItem(
            new Settings().maxCount(1), 2,
            new DrugInfluence("Cannabis", 20, 0.002, 0.001, 0.20f)
    ));

    public static Item cocaSeeds = register("coca_seeds", new AliasedBlockItem(PSBlocks.cocaPlant, new Settings()));
    public static Item cocaLeaf = register("coca_leaf");
    public static Item cocainePowder = register("cocaine_powder", new ItemCocainePowder(new Settings(), new DrugInfluence("Cocaine", 0, 0.002, 0.003, 0.35f)));
    public static Item driedCocaLeaves = register("dried_coca_leaves");

    public static InjectableItem syringe = register("syringe", new InjectableItem(new Settings(), MILLIBUCKETS_PER_LITER / 100));

    public static Item juniperBerries = register("juniper_berries", new ItemFoodSpecial(
            new Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.5F).meat().build()), 15
    ));
    public static Item coffeaCherries = register("coffea_cherries", new AliasedBlockItem(PSBlocks.coffea, new Settings()));
    public static Item coffeeBeans = register("coffee_beans");

    public static Item peyote = register("peyote", PSBlocks.peyote);
    public static Item driedPeyote = register("dried_peyote", new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence("Peyote", 15, 0.005, 0.003, 0.5f)
    ));
    public static Item peyoteJoint = register("peyote_joint", new SmokeableItem(
            new Settings().maxCount(1), 2,
            new DrugInfluence("Peyote", 20, 0.003, 0.0015, 0.4f),
            new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.1f)
    ));

    public static Item lattice = register("lattice", PSBlocks.emptyLattice);
    public static Item bottle_rack = register("bottle_rack", PSBlocks.bottleRack);
    public static Item drying_table = register("drying_table", PSBlocks.dryingTable);
    public static Item iron_drying_table = register("iron_drying_table", PSBlocks.dryingTableIron);

    public static ItemHarmonium harmonium = register("harmonium", new ItemHarmonium(new Settings()));

    public static ItemBong pipe = register("smoking_pipe", new ItemBong(new Settings().maxDamage(50).maxCount(1)))
            .consumes(new ItemBong.Consumable(driedCannabisBuds.getDefaultStack(), new DrugInfluence("Cannabis", 20, 0.002, 0.001, 0.25F)))
            .consumes(new ItemBong.Consumable(driedTobacco.getDefaultStack(), new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.8f)))
            .consumes(new ItemBong.Consumable(harmonium.getDefaultStack(), stack -> new DrugInfluenceHarmonium("Harmonium", 0, 0.04, 0.01, 0.65f, MathUtils.unpackRgb(harmonium.getColor(stack)))));
    // TODO: Play around with the bongs benefits
    public static ItemBong bong = register("bong", new ItemBong(new Settings().maxDamage(128).maxCount(1)))
            .consumes(new ItemBong.Consumable(driedCannabisBuds.getDefaultStack(), new DrugInfluence("Cannabis", 20, 0.002, 0.001, 0.2F)))
            .consumes(new ItemBong.Consumable(driedTobacco.getDefaultStack(), new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.6F)));

    static Item register(String name, Block block) {
        return register(name, new BlockItem(block, new Item.Settings()));
    }

    static Item register(String name) {
        return register(name, new Item(new Item.Settings()));
    }

    static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Psychedelicraft.id(name), item);
    }

    public static void bootstrap() { }
}
