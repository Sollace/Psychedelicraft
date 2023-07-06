package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashSet;
import java.util.Set;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.util.Identifier;

public interface HallucinationTypeKeys {
    Set<Identifier> REGISTRY = new HashSet<>();

    Identifier RASTA_HEAD = register("rasta_head");
    Identifier MULTIPLE_ENTITY = register("multiple_entity");
    Identifier SINGLE_ENTITY = register("single_entity");
    Identifier HOSTILE_VILLAGERS = register("hostile_villagers");
    Identifier FRIENDLY_ZOMBIES = register("friendly_zombies");

    static Identifier register(String name) {
        Identifier id = Psychedelicraft.id(name);
        REGISTRY.add(id);
        return id;
    }
}
