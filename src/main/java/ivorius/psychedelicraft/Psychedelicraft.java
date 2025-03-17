/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.command.*;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.container.VariantMarshal;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Suppliers;

public class Psychedelicraft implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String DEFAULT_NAMESPACE = "psychedelicraft";
    public static final String VANILLA_EXTENSIONS_NAMESPACE = DEFAULT_NAMESPACE + "mc";

    private static final Supplier<PSConfig> CONFIG = Suppliers.memoize(() -> {
        return new PSConfig(FabricLoader.getInstance().getConfigDir().resolve(DEFAULT_NAMESPACE + ".json"));
    });

    public static Supplier<Optional<DrugProperties>> globalDrugProperties = Optional::empty;
    public static Supplier<Optional<HitResult>> crossHairTarget = Optional::empty;

    public static Optional<DrugProperties> getGlobalDrugProperties() {
        return globalDrugProperties.get();
    }

    public static Optional<HitResult> getCrossHairTarget() {
        return crossHairTarget.get();
    }

    public static PSConfig getConfig() {
        return CONFIG.get();
    }

    public static Identifier id(String name) {
        return Identifier.of(DEFAULT_NAMESPACE, name);
    }

    @Override
    public void onInitialize() {
        try {
            getConfig().load();
        } catch (Throwable t) {}
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
        PSEffects.bootstrap();
        PSWorldGen.bootstrap();
        PSGameRules.bootstrap();
        PSCommands.bootstrap();
        PSSounds.bootstrap();
        PSScreenHandlers.bootstrap();
        Channel.bootstrap();
        PSCriteria.bootstrap();
        PSParticles.bootstrap();
        PSDamageTypes.bootstrap();
        VariantMarshal.bootstrap();
    }
}