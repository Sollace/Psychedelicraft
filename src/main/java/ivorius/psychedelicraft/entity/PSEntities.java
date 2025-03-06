/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import java.util.function.Supplier;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.entity.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

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

    EntityType<BoatEntity> JUNIPER_BOAT = register("juniper_boat",
        EntityType.Builder.create(getBoatFactory(() -> PSItems.JUNIPER_BOAT), SpawnGroup.MISC)
            .dropsNothing()
            .dimensions(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .maxTrackingRange(10));
    EntityType<ChestBoatEntity> JUNIPER_CHEST_BOAT = register("juniper_chest_boat",
        EntityType.Builder.create(getChestBoatFactory(() -> PSItems.JUNIPER_CHEST_BOAT), SpawnGroup.MISC)
            .dropsNothing()
            .dimensions(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .maxTrackingRange(10)
    );

    private static EntityType.EntityFactory<BoatEntity> getBoatFactory(Supplier<Item> itemSupplier) {
        return (type, world) -> new BoatEntity(type, world, itemSupplier);
    }

    private static EntityType.EntityFactory<ChestBoatEntity> getChestBoatFactory(Supplier<Item> itemSupplier) {
        return (type, world) -> new ChestBoatEntity(type, world, itemSupplier);
    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        var key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Psychedelicraft.id(name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    static void bootstrap() {
        PSTradeOffers.bootstrap();
    }
}
