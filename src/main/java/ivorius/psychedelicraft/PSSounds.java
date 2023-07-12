/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import net.minecraft.util.registry.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public interface PSSounds {

    SoundEvent ENTITY_PLAYER_HEARTBEAT = register("entity.player.heartbeat");
    SoundEvent ENTITY_PLAYER_BREATH = register("entity.player.breath");

    SoundEvent DRUG_GENERIC = register("drug.generic");

    static SoundEvent register(String name) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    static void bootstrap() {}
}