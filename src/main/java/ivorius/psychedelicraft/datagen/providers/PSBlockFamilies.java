package ivorius.psychedelicraft.datagen.providers;

import ivorius.psychedelicraft.block.PSBlocks;
import net.minecraft.data.family.BlockFamily;

public interface PSBlockFamilies {
    BlockFamily JUNIPER = new BlockFamily.Builder(PSBlocks.JUNIPER_PLANKS)
            .slab(PSBlocks.JUNIPER_SLAB).stairs(PSBlocks.JUNIPER_STAIRS).fence(PSBlocks.JUNIPER_FENCE).fenceGate(PSBlocks.JUNIPER_FENCE_GATE)
            .button(PSBlocks.JUNIPER_BUTTON).pressurePlate(PSBlocks.JUNIPER_PRESSURE_PLATE).sign(PSBlocks.JUNIPER_SIGN, PSBlocks.JUNIPER_WALL_SIGN)
            .door(PSBlocks.JUNIPER_DOOR).trapdoor(PSBlocks.JUNIPER_TRAPDOOR)
            .group("wooden").unlockCriterionName("has_planks")
            .build();
}
