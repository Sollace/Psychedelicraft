/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.commands.*;
import ivorius.psychedelicraft.crafting.PSRecipes;
import ivorius.psychedelicraft.entities.PSEntityList;
import ivorius.psychedelicraft.items.PSItemGroups;
import ivorius.psychedelicraft.items.PSItems;
import ivorius.psychedelicraft.worldgen.PSWorldGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Psychedelicraft implements ModInitializer {
    public static final Logger logger = LogManager.getLogger();

    public static final String filePathTextures = "textures/mod/";
    public static final String filePathModels = "models/";
    public static final String filePathOther = "other/";
    public static final String filePathShaders = "shaders/";

    public static Identifier id(String name) {
        return new Identifier("psychedelicraft", name);
    }

    @Override
    public void onInitialize() {
        PSBlocks.bootstrap();
        PSItems.bootstrap();
        PSItemGroups.bootstrap();
        PSRecipes.bootstrap();
        PSEntityList.bootstrap();
        PSWorldGen.bootstrap();
        PSCommands.bootstrap();
    }
}