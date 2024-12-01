package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class PSEntityTypeTagProvider extends FabricTagProvider<EntityType<?>> {
    public PSEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, RegistryKeys.ENTITY_TYPE, completableFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(PSTags.Entities.SINGLE_ENTITY_HALLUCINATIONS).add(
                EntityType.CREEPER,
                EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
                EntityType.BLAZE, EntityType.BREEZE,
                EntityType.ENDERMAN,
                EntityType.COW, EntityType.MOOSHROOM,
                EntityType.SHEEP,
                EntityType.PIG, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN,
                EntityType.OCELOT, EntityType.CAT,
                EntityType.WOLF, EntityType.FOX,
                EntityType.SILVERFISH, EntityType.ENDERMITE,
                EntityType.VILLAGER, EntityType.ILLUSIONER, EntityType.PILLAGER, EntityType.WANDERING_TRADER,
                EntityType.BOGGED, EntityType.SKELETON, EntityType.WITHER_SKELETON,
                EntityType.IRON_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.HORSE, EntityType.MULE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE,
                EntityType.ALLAY,
                EntityType.AXOLOTL, EntityType.FROG,
                EntityType.RABBIT, EntityType.CAMEL,
                EntityType.DOLPHIN,
                EntityType.WITHER, EntityType.WARDEN, EntityType.RAVAGER
        );
        getOrCreateTagBuilder(PSTags.Entities.MULTIPLE_ENTITY_HALLUCINATIONS).add(
                EntityType.SQUID, EntityType.GLOW_SQUID,
                EntityType.DOLPHIN,
                EntityType.COD,
                EntityType.SALMON,
                EntityType.TROPICAL_FISH,
                EntityType.TURTLE,
                EntityType.PARROT, EntityType.BAT, EntityType.BEE
        );
    }
}
