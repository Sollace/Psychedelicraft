/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.commands.*;
import ivorius.psychedelicraft.crafting.PSRecipes;
import ivorius.psychedelicraft.entities.PSEntities;
import ivorius.psychedelicraft.items.PSItemGroups;
import ivorius.psychedelicraft.items.PSItems;
import ivorius.psychedelicraft.worldgen.PSWorldGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Psychedelicraft implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String TEXTURES_PATH = "textures/mod/";
    public static final String MODELS_PATH = "models/";
    public static final String OTHER_PATH = "other/";
    public static final String SHADERS_PATH = "shaders/";

    public static Identifier id(String name) {
        return new Identifier("psychedelicraft", name);
    }

    @Override
    public void onInitialize() {
        PSBlocks.bootstrap();
        PSItems.bootstrap();
        PSItemGroups.bootstrap();
        PSRecipes.bootstrap();
        PSEntities.bootstrap();
        PSWorldGen.bootstrap();
        PSCommands.bootstrap();
    }
}