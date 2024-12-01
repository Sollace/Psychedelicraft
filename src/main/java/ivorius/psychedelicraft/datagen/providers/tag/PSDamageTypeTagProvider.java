package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.PSTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.DamageTypeTags;

public class PSDamageTypeTagProvider extends FabricTagProvider<DamageType> {
    public PSDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, completableFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {

        getOrCreateTagBuilder(PSTags.DamageTypes.IS_BIOLOGICAL).add(
                PSDamageTypes.ALCOHOL_POSIONING,
                PSDamageTypes.RESPIRATORY_FAILURE,
                PSDamageTypes.STROKE,
                PSDamageTypes.HEART_FAILURE,
                PSDamageTypes.HEART_ATTACK,
                PSDamageTypes.KIDNEY_FAILURE,
                PSDamageTypes.IN_SLEEP,
                PSDamageTypes.OVER_EATING
        );
        getOrCreateTagBuilder(PSTags.DamageTypes.IS_INCENDIARY).add(
                PSDamageTypes.MOLOTOV,
                PSDamageTypes.SELF_MOLOTOV
        );

        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
        getOrCreateTagBuilder(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_COOLDOWN).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_EFFECTS).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_RESISTANCE).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS).forceAddTag(PSTags.DamageTypes.IS_BIOLOGICAL);
        getOrCreateTagBuilder(DamageTypeTags.IS_FIRE).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
        getOrCreateTagBuilder(DamageTypeTags.IS_EXPLOSION).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
        getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE).forceAddTag(PSTags.DamageTypes.IS_INCENDIARY);
    }
}
