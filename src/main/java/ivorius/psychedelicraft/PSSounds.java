/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.registry.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public interface PSSounds {

    SoundEvent ENTITY_PLAYER_HEARTBEAT = register("entity.player.heartbeat");
    SoundEvent ENTITY_PLAYER_BREATH = register("entity.player.breath");

    SoundEvent DRUG_ALCOHOL = register("drug.alcohol");
    SoundEvent DRUG_CANNABIS = register("drug.cannabis");
    SoundEvent DRUG_BROWNSHROOMS = register("drug.brownshrooms");
    SoundEvent DRUG_REDSHROOMS = register("drug.reedshrooms");
    SoundEvent DRUG_TOBACCO = register("drug.tobacco");
    SoundEvent DRUG_COCAINE = register("drug.cocaine");
    SoundEvent DRUG_CAFFIENE = register("drug.caffiene");
    SoundEvent DRUG_WARMTH = register("drug.warmth");
    SoundEvent DRUG_PEYOTE = register("drug.peyote");
    SoundEvent DRUG_ZERO = register("drug.zero");
    SoundEvent DRUG_HARMONIUM = register("drug.harmonium");

    static SoundEvent register(String name) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void bootstrap() {}
}