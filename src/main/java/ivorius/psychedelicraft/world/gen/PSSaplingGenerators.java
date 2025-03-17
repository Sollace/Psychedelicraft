package ivorius.psychedelicraft.world.gen;

import java.util.Optional;

import net.minecraft.block.SaplingGenerator;

public interface PSSaplingGenerators {
    SaplingGenerator JUNIPER = new SaplingGenerator("psychedelicraft:juniper",
            Optional.empty(),
            Optional.of(PSFeatureConfigs.JUNIPER_TREE),
            Optional.empty()
    );
}
