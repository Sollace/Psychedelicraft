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
    SoundEvent ENTITY_PLAYER_PACIFIER_SQUEAK = register("entity.player.pacifier.squeak");

    SoundEvent BLOCK_RIFT_JAR_TOGGLE = register("block.rift_jar.toggle");
    SoundEvent BLOCK_RIFT_JAR_OPEN = register("block.rift_jar.open");
    SoundEvent BLOCK_RIFT_JAR_CLOSE = register("block.rift_jar.close");

    SoundEvent BLOCK_TRAY_HARDEN = register("block.tray.harden");

    SoundEvent BLOCK_VALVE_OPEN = register("block.valve.open");
    SoundEvent BLOCK_VALVE_CLOSE = register("block.valve.close");

    SoundEvent ITEM_SYRINGE_INJECT = register("item.syringe.inject");

    SoundEvent BLOCK_BUNSEN_BURNER_WORK = register("block.bunsen_burner.work");
    SoundEvent BLOCK_BUNSEN_BURNER_OVERHEAT = register("block.bunsen_burner.overheat");
    SoundEvent BLOCK_BUNSEN_BURNER_FILL = register("block.bunsen_burner.fill");

    static SoundEvent register(String name) {
        Identifier id = Psychedelicraft.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void bootstrap() {}
}