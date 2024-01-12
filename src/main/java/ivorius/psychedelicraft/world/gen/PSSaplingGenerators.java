package ivorius.psychedelicraft.world.gen;

import java.util.Optional;

import net.minecraft.block.SaplingGenerator;

public interface PSSaplingGenerators {
    SaplingGenerator JUNIPER = new SaplingGenerator("psychedelicraft:juniper", Optional.empty(), Optional.of(PSWorldGen.JUNIPER_TREE_CONFIG), Optional.empty());
}
