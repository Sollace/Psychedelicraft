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

    SoundEvent BLOCK_RIFT_JAR_TOGGLE = register("block.rift_jar.toggle");
    SoundEvent BLOCK_RIFT_JAR_OPEN = register("block.rift_jar.open");
    SoundEvent BLOCK_RIFT_JAR_CLOSE = register("block.rift_jar.close");

    SoundEvent ITEM_SYRINGE_INJECT = register("item.syringe.inject");

    SoundEvent DRUG_GENERIC = register("drug.generic");

    static SoundEvent register(String name) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void bootstrap() {}
}