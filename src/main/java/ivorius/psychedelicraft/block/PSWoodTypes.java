package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeRegistry;
import net.minecraft.block.WoodType;
import net.minecraft.util.Identifier;

public interface PSWoodTypes {
    WoodType JUNIPER = register("juniper");

    static WoodType register(String name) {
        Identifier id = Psychedelicraft.id(name);
        return WoodTypeRegistry.register(id, BlockSetTypeRegistry.registerWood(id));
    }
}
