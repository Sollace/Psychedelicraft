/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import static ivorius.psychedelicraft.fluids.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.entities.drugs.DrugInfluenceHarmonium;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 1 Jan 2023
 */
public class PSItems {
    public static ItemCup woodenMug = register("wooden_mug", new ItemCupWithLiquid(MILLIBUCKETS_PER_LITER / 2));
    public static ItemCup stoneCup = register("stone_cup", new ItemCupWithLiquid(MILLIBUCKETS_PER_LITER / 20));
    public static ItemCup glassChalice = register("glass_chalace", new ItemCupWithLiquid(MILLIBUCKETS_PER_LITER / 5));

    public static ItemBarrel itemBarrel = register("oak_barrel", new ItemBarrel(PSBlocks.oak_barrel));
    public static ItemBarrel itemSpruceBarrel = register("spruce_barrel", new ItemBarrel(PSBlocks.spruce_barrel));
    public static ItemBarrel itemBirchBarrel = register("birch_barrel", new ItemBarrel(PSBlocks.birch_barrel));
    public static ItemBarrel itemJungleBarrel = register("jungle_barrel", new ItemBarrel(PSBlocks.jungle_barrel));
    public static ItemBarrel itemAcaciaBarrel = register("acacia_barrel", new ItemBarrel(PSBlocks.acacia_barrel));
    public static ItemBarrel itemDarkOakBarrel = register("dark_oak_barrel", new ItemBarrel(PSBlocks.dark_oak_barrel));
    public static ItemMashTub itemMashTub = register("mash_rub", new ItemMashTub(PSBlocks.mashTub));
    public static ItemFlask itemFlask = register("flask", new ItemFlask(PSBlocks.flask));
    public static ItemDistillery itemDistillery = register("distillery", new ItemDistillery(PSBlocks.distillery));

    public static Item wineGrapes = register("wine_grapes", new ItemWineGrapes(new Settings().food(
            new FoodComponent.Builder().hunger(1).saturationModifier(0.5F).meat().build()
    ), 15));
    public static ItemBottleDrinkable bottle = register("bottle", new ItemBottleDrinkable(MILLIBUCKETS_PER_LITER * 2));
    public static ItemMolotovCocktail molotovCocktail = register("molotov_cocktail", new ItemMolotovCocktail(MILLIBUCKETS_PER_LITER * 2));

    public static Item cannabisSeeds = register("cannabis_seeds", new AliasedBlockItem(PSBlocks.cannabisPlant, new Settings()));
    public static Item cannabisLeaf = register("cannabis_leaf");
    public static Item cannabisBuds = register("cannabis_buds");
    public static Item driedCannabisBuds = register("dried_cannabis_buds");
    public static Item driedCannabisLeaves = register("dried_cannabis_leaves");
    public static ItemBong pipe = register("smoking_pipe", new ItemBong(new Settings().maxDamage(50).maxCount(1)));
    public static ItemBong bong = register("bong", new ItemBong(new Settings().maxDamage(128).maxCount(1)));
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

    public static Item harmonium = register("harmonium", new ItemHarmonium(new Settings()));

    static Item register(String name, Block block) {
        return register(name, new BlockItem(block, new Item.Settings()));
    }

    static Item register(String name) {
        return register(name, new Item(new Item.Settings()));
    }

    static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Psychedelicraft.id(name), item);
    }

    public static void bootstrap() {
        pipe.addConsumable(new ItemBong.Consumable(new ItemStack(driedCannabisBuds), new DrugInfluence("Cannabis", 20, 0.002, 0.001, 0.25f)));
        pipe.addConsumable(new ItemBong.Consumable(new ItemStack(driedTobacco), new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.8f)));

        // TODO: Play around with the bongs benefits
        bong.addConsumable(new ItemBong.Consumable(new ItemStack(driedCannabisBuds), new DrugInfluence("Cannabis", 20, 0.002, 0.001, 0.2f)));
        bong.addConsumable(new ItemBong.Consumable(new ItemStack(driedTobacco), new DrugInfluence("Tobacco", 0, 0.1, 0.02, 0.6f)));

        for (int i = 0; i < 16; i++) {
            // TODO: (Sollace) colour should be gotten from the item NBT
            float[] color = SheepEntity.getRgbColor(DyeColor.values()[i]);
            pipe.addConsumable(new ItemBong.Consumable(new ItemStack(harmonium), new DrugInfluenceHarmonium("Harmonium", 0, 0.04, 0.01, 0.65f, color)));
        }
    }
}
