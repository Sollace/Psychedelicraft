package ivorius.psychedelicraft.world.gen.loot;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSLootFunctionTypes {
    LootFunctionType<SetFluidsLootFunction> SET_FLUIDS = register("set_fluids", SetFluidsLootFunction.CODEC);

    private static <T extends LootFunction> LootFunctionType<T> register(String name, MapCodec<T> codec) {
        return Registry.register(Registries.LOOT_FUNCTION_TYPE, Psychedelicraft.id(name), new LootFunctionType<>(codec));
    }

    static void bootstrap() {}
}
