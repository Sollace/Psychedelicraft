/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.command.*;
import ivorius.psychedelicraft.config.JsonConfig;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItemGroups;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import ivorius.psychedelicraft.world.gen.PSWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Psychedelicraft implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    private static final Supplier<JsonConfig.Loader<PSConfig>> CONFIG_LOADER = JsonConfig.create("psychedelicraft.json", PSConfig::new);

    public static Supplier<Optional<DrugProperties>> globalDrugProperties = Optional::empty;

    public static Optional<DrugProperties> getGlobalDrugProperties() {
        return globalDrugProperties.get();
    }

    public static PSConfig getConfig() {
        return CONFIG_LOADER.get().getData();
    }

    public static Identifier id(String name) {
        return new Identifier("psychedelicraft", name);
    }

    @Override
    public void onInitialize() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            DrugProperties.of(player).sendCapabilities();
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            DrugProperties.of(newPlayer).copyFrom(DrugProperties.of(oldPlayer), alive);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DrugProperties.of(handler.player).sendCapabilities();
        });

        PSBlocks.bootstrap();
        PSItems.bootstrap();
        PSTags.bootstrap();
        PSItemGroups.bootstrap();
        PSFluids.bootstrap();
        PSRecipes.bootstrap();
        PSEntities.bootstrap();
        PSWorldGen.bootstrap();
        PSCommands.bootstrap();
        PSSounds.bootstrap();
        PSScreenHandlers.bootstrap();
        Channel.bootstrap();
        PSCriteria.bootstrap();
        PSParticles.bootstrap();
    }
}