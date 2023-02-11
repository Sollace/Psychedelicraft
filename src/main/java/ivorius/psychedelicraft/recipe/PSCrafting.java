/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

/**
 * Created by lukas on 18.10.14.
 */
public class PSCrafting {
    public static void initialize() {
        // TODO: (Sollace)
        //       1.  StainedGlassRecipe (crafting a bottle from the different colours of stained glass)
        //           empty_bottle.json
        //                # = stained glass
        //               # #                -> 8x bottle (empty, keeps colour)
        //               ###
        //           empty_molotov_cocktail.json
        //                P = paper
        //                # = stained glass -> 4x molotov (empty, keeps colour)

        //for (int color = 0; color < ItemDye.field_150922_c.length; color++) {
        //    addRecipe(new ItemStack(molotovCocktail, 4, color), "P", "#", '#', new ItemStack(stained_glass, 1, 15 - color), 'P', paper);
        //    addRecipe(new ItemStack(bottle, 8, color), " # ", "# #", "###", '#', new ItemStack(stained_glass, 1, 15 - color));
        //}
    }
}
