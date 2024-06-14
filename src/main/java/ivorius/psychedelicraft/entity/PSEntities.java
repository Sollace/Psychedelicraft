/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.entity.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Created by lukas on 25.04.14.
 *
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSEntities {
    EntityType<MolotovCocktailEntity> MOLOTOV_COCKTAIL = register("molotov_cocktail", EntityType.Builder.<MolotovCocktailEntity>create(MolotovCocktailEntity::new, SpawnGroup.MISC)
            .alwaysUpdateVelocity(true)
            .trackingTickInterval(10)
            .maxTrackingRange(4)
            .dimensions(0.1F, 0.1F));
    EntityType<RealityRiftEntity> REALITY_RIFT = register("reality_rift", EntityType.Builder.create(RealityRiftEntity::new, SpawnGroup.MISC)
            .trackingTickInterval(3)
            .maxTrackingRange(5)
            .dimensions(2F, 2F));

    TerraformBoatType JUNIPER_BOAT_TYPE = Registry.register(TerraformBoatTypeRegistry.INSTANCE, Psychedelicraft.id("juniper"), new TerraformBoatType.Builder()
            .planks(PSItems.JUNIPER_PLANKS)
            .item(PSItems.JUNIPER_BOAT)
            .build());

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registries.ENTITY_TYPE, Psychedelicraft.id(name), type);
    }

    static void bootstrap() {
        PSTradeOffers.bootstrap();



    }
}
