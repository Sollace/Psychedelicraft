package ivorius.psychedelicraft.entity.effect;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public interface PSEffects {
    RegistryEntry<StatusEffect> TEETH_GRINDING = register("teeth_grinding", new StatusEffect(StatusEffectCategory.HARMFUL, 0) {});

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Psychedelicraft.id(name), effect);
    }

    static void bootstrap() {}
}
