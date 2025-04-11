/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 25.04.14.
 *
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSEntities {
    EntityType<MolotovCocktailEntity> MOLOTOV_COCKTAIL = register("molotov_cocktail", FabricEntityTypeBuilder.<MolotovCocktailEntity>create(SpawnGroup.MISC, MolotovCocktailEntity::new)
            .forceTrackedVelocityUpdates(true)
            .trackedUpdateRate(10)
            .trackRangeChunks(4)
            .dimensions(EntityDimensions.fixed(0.1F, 0.1F)));
    EntityType<RealityRiftEntity> REALITY_RIFT = register("reality_rift", FabricEntityTypeBuilder.create(SpawnGroup.MISC, RealityRiftEntity::new)
            .trackedUpdateRate(3)
            .trackRangeChunks(5)
            .dimensions(EntityDimensions.changing(2F, 2F)));

    TerraformBoatType JUNIPER_BOAT_TYPE = Registry.register(TerraformBoatTypeRegistry.INSTANCE, Psychedelicraft.id("juniper"), new TerraformBoatType.Builder()
            .planks(PSItems.JUNIPER_PLANKS)
            .item(PSItems.JUNIPER_BOAT)
            .build());

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registries.ENTITY_TYPE, id, builder.build());
    }

    static void bootstrap() {
        PSTradeOffers.bootstrap();



    }
}
