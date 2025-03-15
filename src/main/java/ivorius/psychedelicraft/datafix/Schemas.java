package ivorius.psychedelicraft.datafix;

import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

public interface Schemas {
    static void fixComponents(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic) {
        PSItemStackComponentizations.run(data, dynamic);
    }
}
