/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.entities.drugs.DrugInfluenceHarmonium;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.items.ItemBong;
import net.minecraft.item.ItemStack;

import java.util.List;

import static ivorius.psychedelicraft.blocks.PSBlocks.*;
import static ivorius.psychedelicraft.blocks.TileEntityMashTub.MASH_TUB_CAPACITY;
import static ivorius.psychedelicraft.crafting.OreDictionaryConstants.*;
import static ivorius.psychedelicraft.fluids.PSFluids.*;
import static ivorius.psychedelicraft.items.PSItems.*;

/**
 * Created by lukas on 18.10.14.
 */
public class PSCrafting
{
    public static void initialize()
    {
        RecipeSorter.register("ConvertFluidContainer", RecipeConvertFluidContainer.class, RecipeSorter.Category.SHAPED, "");
        RecipeSorter.register("FillDrink", RecipeFillDrink.class, RecipeSorter.Category.SHAPELESS, "");
        RecipeSorter.register("RecipeAction", RecipeActionRepresentation.class, RecipeSorter.Category.SHAPELESS, "");

        //-----

        addRecipe(new ItemStack(syringe), "I", "#", 'I', DC_IRON_INGOT, '#', DC_BLOCK_GLASS_CLEAR);
        addRecipe(new ItemStack(pipe), "  I", " S ", "WS ", 'I', DC_IRON_INGOT, 'S', DC_STICK_WOOD, 'W', DC_PLANK_WOOD);
        addRecipe(new ItemStack(bong, 1, 3), " P ", "G G", "GGG", 'P', DC_GLASS_PANE_CLEAR, 'G', DC_BLOCK_GLASS_CLEAR);

        for (int type = 0; type < BlockWood.field_150096_a.length; type++)
        {
            addRecipe(new ItemStack(itemBarrel, 1, type), " I ", "# #", "S#S", '#', new ItemStack(planks, 1, type), 'I', DC_IRON_INGOT, 'S', DC_STICK_WOOD);
        }

        addRecipe(new ItemStack(itemMashTub), "# #", "I I", "###", 'I', DC_IRON_INGOT, '#', DC_PLANK_WOOD);

        Object flaskIngotItem = foreignItem(DC_COPPER_INGOT, DC_IRON_INGOT);
        addRecipe(new ItemStack(itemFlask), " # ", "#G#", "###", 'G', DC_BLOCK_GLASS_CLEAR, '#', flaskIngotItem);
        addRecipe(new ItemStack(itemDistillery), "##", "D ", 'D', itemFlask, '#', flaskIngotItem);

        addMashTubRecipe(new FluidStack(alcWheatHop, MASH_TUB_CAPACITY), water_bucket, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CONES_HOPS, DC_CONES_HOPS);
        addMashTubRecipe(new FluidStack(alcWheat, MASH_TUB_CAPACITY), water_bucket, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT, DC_CROP_WHEAT);
        addMashTubRecipe(new FluidStack(alcCorn, MASH_TUB_CAPACITY), water_bucket, DC_CORN, DC_CORN, DC_CORN, DC_CORN, DC_CORN, DC_CORN, DC_CORN);
        addMashTubRecipe(new FluidStack(alcRedGrapes, MASH_TUB_CAPACITY), DC_GRAPE, DC_GRAPE, DC_GRAPE, DC_GRAPE, DC_GRAPE, DC_GRAPE, DC_GRAPE, DC_GRAPE);
        addMashTubRecipe(new FluidStack(alcRice, MASH_TUB_CAPACITY), water_bucket, DC_RICE, DC_RICE, DC_RICE, DC_RICE, DC_RICE, DC_RICE, DC_RICE);
        addMashTubRecipe(new FluidStack(alcHoney, MASH_TUB_CAPACITY), water_bucket, DC_HONEY_DROP, DC_HONEY_DROP, DC_HONEY_DROP, DC_HONEY_DROP, DC_HONEY_DROP, DC_HONEY_DROP, DC_HONEY_DROP);
        addMashTubRecipe(new FluidStack(alcJuniper, MASH_TUB_CAPACITY), DC_JUNIPER_BERRIES, DC_JUNIPER_BERRIES, DC_JUNIPER_BERRIES, DC_JUNIPER_BERRIES, sugar, DC_GRAPE, DC_GRAPE, DC_CROP_WHEAT);
        addMashTubRecipe(new FluidStack(alcPotato, MASH_TUB_CAPACITY), water_bucket, DC_POTATO, DC_POTATO, DC_POTATO, DC_POTATO, DC_POTATO, DC_POTATO, DC_POTATO);
        addMashTubRecipe(new FluidStack(alcSugarCane, MASH_TUB_CAPACITY), water_bucket, Items.reeds, Items.reeds, Items.reeds, Items.reeds, Items.reeds, Items.reeds, Items.reeds);
        addMashTubRecipe(new FluidStack(alcApple, MASH_TUB_CAPACITY), apple, apple, apple, apple, apple, apple, apple, apple);
        addMashTubRecipe(new FluidStack(alcPineapple, MASH_TUB_CAPACITY), DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE, DC_PINEAPPLE);
        addMashTubRecipe(new FluidStack(alcBanana, MASH_TUB_CAPACITY), DC_BANANA, DC_BANANA, DC_BANANA, DC_BANANA, DC_BANANA, DC_BANANA, DC_BANANA, DC_BANANA);
        addMashTubRecipe(new FluidStack(alcMilk, MASH_TUB_CAPACITY), milk_bucket, milk_bucket, milk_bucket, milk_bucket, milk_bucket, milk_bucket, milk_bucket, milk_bucket);

        addRecipe(new ItemStack(glassChalice, 4), "# #", " # ", " # ", '#', DC_BLOCK_GLASS_CLEAR);
        addRecipe(new ItemStack(woodenMug, 8), "# #", "# #", "###", '#', DC_PLANK_WOOD);
        addRecipe(new ItemStack(stoneCup, 8), "# #", "# #", "###", '#', clay_ball);

        addRecipe(new ItemStack(wineGrapeLattice), "III", "IOI", "OIO", 'I', DC_STICK_WOOD, 'O', DC_PLANK_WOOD);

        addRecipe(new ItemStack(bottleRack), "I#I", "#I#", "I#I", '#', DC_SINGLE_PLANK_WOOD_REFINED, 'I', DC_STICK_WOOD);

//        for (int color = 0; color < ItemDye.field_150922_c.length; color++)
//            addRecipe(new ItemStack(molotovCocktail, 4, color), "P", "#", '#', new ItemStack(stained_glass, 1, 15 - color), 'P', paper);

        for (int color = 0; color < ItemDye.field_150922_c.length; color++)
        {
            addRecipe(new ItemStack(bottle, 8, color), " # ", "# #", "###", '#', new ItemStack(stained_glass, 1, 15 - color));
            GameRegistry.addRecipe(new RecipeConvertFluidContainer(new ItemStack(molotovCocktail, 1, color), wool, new ItemStack(bottle, 1, color)));
            GameRegistry.addRecipe(new RecipeConvertFluidContainer(new ItemStack(bottle, 1, color), new ItemStack(molotovCocktail, 1, color)));
        }

        addRecipe(new ItemStack(dryingTable), "###", "#R#", '#', DC_PLANK_WOOD, 'R', DC_REDSTONE_DUST);
        addRecipe(new ItemStack(dryingTableIron), "#I#", "IRI", '#', DC_PLANK_WOOD, 'R', DC_REDSTONE_DUST, 'I', DC_IRON_INGOT);

        DryingRegistry.addDryingResult(DC_LEAF_CANNABIS, new ItemStack(driedCannabisLeaves, 3));
        DryingRegistry.addDryingResult(DC_BUD_CANNABIS, new ItemStack(driedCannabisBuds, 3));
        addRecipe(new ItemStack(hashMuffin), "LLL", "#X#", "LLL", 'X', new ItemStack(dye, 1, 3), '#', DC_CROP_WHEAT, 'L', DC_DRIED_CANNABIS_LEAVES);
        addRecipe(new ItemStack(joint), "P", "C", "P", 'P', paper, 'C', driedCannabisBuds);


        DryingRegistry.addDryingResult(brown_mushroom, new ItemStack(magicMushroomsBrown, 3));
        DryingRegistry.addDryingResult(red_mushroom, new ItemStack(magicMushroomsRed, 3));

        addShapelessRecipe(new ItemStack(cocainePowder), driedCocaLeaves);

        addRecipe(new ItemStack(cigarette, 4), "P", "T", "P", 'P', paper, 'T', DC_DRIED_TOBACCO);
        addRecipe(new ItemStack(cigar), "TTT", "TTT", "PPP", 'P', paper, 'T', DC_DRIED_TOBACCO);
        DryingRegistry.addDryingResult(DC_LEAF_TOBACCO, new ItemStack(driedTobacco, 3));

        DryingRegistry.addDryingResult(DC_LEAF_COCA, new ItemStack(driedCocaLeaves, 3));

        addRecipe(new ItemStack(planks, 4, 1), "#", '#', psycheLog);

        GameRegistry.addSmelting(coffeaCherries, new ItemStack(coffeeBeans), 0.2f);

        DryingRegistry.addDryingResult(DC_CROP_PEYOTE, new ItemStack(driedPeyote, 3));
        addRecipe(new ItemStack(peyoteJoint), "P", "D", "P", 'P', paper, 'D', driedPeyote);

        for (int i = 0; i < 16; i++)
        {
            if (PSConfig.enableHarmonium)
                addShapelessRecipe(new ItemStack(harmonium, 1, i), new ItemStack(dye, 1, i), DC_GLOWSTONE_DUST, DC_DRIED_TOBACCO);
        }

        if (PSConfig.enableRiftJars)
            addRecipe(new ItemStack(riftJar), "O-O", "GO ", "OIO", 'O', DC_BLOCK_GLASS_CLEAR, '-', DC_PLANK_WOOD, 'G', DC_GOLD_INGOT, 'I', DC_IRON_INGOT);

        RecipeActionRegistry.addRecipe("ps_pourDrink", new RecipePourDrink());

        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(peyoteJuice, FluidHelper.MILLIBUCKETS_PER_LITER / 2), water_bucket, DC_PEYOTE_DRIED, DC_PEYOTE_DRIED));
        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(coffee, FluidHelper.MILLIBUCKETS_PER_LITER / 2), water_bucket, DC_COFFEE_BEANS, DC_COFFEE_BEANS));
        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(cocaTea, FluidHelper.MILLIBUCKETS_PER_LITER / 2), water_bucket, DC_LEAF_COCA, DC_LEAF_COCA));
        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(cannabisTea, FluidHelper.MILLIBUCKETS_PER_LITER / 2), water_bucket, DC_LEAF_CANNABIS, DC_LEAF_CANNABIS));
        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(cocaineFluid, FluidHelper.MILLIBUCKETS_PER_LITER / 100), water_bucket, DC_COCAINE_POWDER));
        GameRegistry.addRecipe(new RecipeFillDrink(new FluidStack(caffeineFluid, FluidHelper.MILLIBUCKETS_PER_LITER / 100), DC_COFFEE_BEANS, DC_COFFEE_BEANS));

        //TODO Add when Forge fixes smelting with NBT
//                GameRegistry.addSmelting(DrinkRegistry.createDrinkStack(itemDrinkHolder, 1, "coldCoffee"), new ItemStack(emptyContainer, 1, 3), 0.2f);
    }

    private static Object foreignItem(String oredictID, Object replacement)
    {
        List<ItemStack> foreignItems = OreDictionary.getOres(oredictID);
        return foreignItems.size() == 0 ? replacement : oredictID;
    }

    private static void addRecipe(ItemStack output, Object... params)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(output, params));
    }

    private static void addShapelessRecipe(ItemStack output, Object... params)
    {
        GameRegistry.addRecipe(new ShapelessOreRecipe(output, params));
    }

    private static void addMashTubRecipe(FluidStack fluid, Object... ingredients)
    {
        ItemStack mashTubStack = new ItemStack(itemMashTub);
        itemMashTub.fill(mashTubStack, fluid, true);

        Object[] ing = new Object[ingredients.length + 1];
        System.arraycopy(ingredients, 0, ing, 1, ingredients.length);
        ing[0] = new ItemStack(itemMashTub);

        addShapelessRecipe(mashTubStack, ing);
    }
}
