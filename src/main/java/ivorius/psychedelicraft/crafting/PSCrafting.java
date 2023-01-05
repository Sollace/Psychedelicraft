/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

/**
 * Created by lukas on 18.10.14.
 */
public class PSCrafting {
    public static void initialize() {
        // TODO: (Sollace) Move recipes to resources
        // TODO: (Sollace) Molotov cocktail recipes (dyed items recipe)
        //    -- Update: We don't need to add our own recipes! Just implement DyeableItem on the items we want dyed!
        //            Items so far that be dyeable:
        //                                   molotov_cocktail, bottle, harmonium
        //
        //     -- Update 2: Or do we?
        //         USE FOR THE BELOW:         ConvertDrinkContainerRecipe
        //         Recipe seems to go
        //                           P - paper
        //                           # - stained glass        ----------> molotov cocktail (with colour)
        //                           # - stained glass
        //
        //
        //                           #
        //                          # # - stained glass       -----------> empty bottle (with colour) (normal recipe)
        //                          # #
        //
        //
        //                           B - molotov cocktail (with colour) -----------> bottle (keeps colour) (keeps fluid)
        //                           # - wool
        //
        //                           B - bottle (with colour) -----------> molotov cocktail (keeps colour) (after combining with fluid)
//        for (int color = 0; color < ItemDye.field_150922_c.length; color++)
//            addRecipe(new ItemStack(molotovCocktail, 4, color), "P", "#", '#', new ItemStack(stained_glass, 1, 15 - color), 'P', paper);
        //for (int color = 0; color < ItemDye.field_150922_c.length; color++)
        //{
        //    addRecipe(new ItemStack(bottle, 8, color), " # ", "# #", "###", '#', new ItemStack(stained_glass, 1, 15 - color));
        //    GameRegistry.addRecipe(new RecipeConvertFluidContainer(new ItemStack(molotovCocktail, 1, color), wool, new ItemStack(bottle, 1, color)));
        //    GameRegistry.addRecipe(new RecipeConvertFluidContainer(new ItemStack(bottle, 1, color), new ItemStack(molotovCocktail, 1, color)));
        //}

        //if (PSConfig.enableHarmonium)
        // if (PSConfig.enableRiftJars)
    }
}
