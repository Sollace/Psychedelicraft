package ivorius.psychedelicraft.world.gen.loot;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.JsonSerializer;

public interface PSLootFunctionTypes {
    LootFunctionType SET_FLUIDS = register("set_fluids", new SetFluidsLootFunction.Serializer());

    private static <T extends LootFunction> LootFunctionType register(String name, JsonSerializer<T> serializer) {
        return Registry.register(Registries.LOOT_FUNCTION_TYPE, Psychedelicraft.id(name), new LootFunctionType(serializer));
    }

    static void bootstrap() {}
}
