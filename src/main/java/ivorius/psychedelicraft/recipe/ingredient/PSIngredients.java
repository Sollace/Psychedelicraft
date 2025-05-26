package ivorius.psychedelicraft.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface PSIngredients {
    CustomIngredientSerializer<FluidIngredient> FLUID = register("fluid", FluidIngredient.MAP_CODEC, FluidIngredient.PACKET_CODEC);
    CustomIngredientSerializer<OptionalFluidIngredient> OPTIONAL_FLUID = register("optional_fluid", OptionalFluidIngredient.CODEC, OptionalFluidIngredient.PACKET_CODEC);

    private static <T extends CustomIngredient> CustomIngredientSerializer<T> register(String name, MapCodec<T> codec, PacketCodec<PacketByteBuf, T> packetCodec) {
        var serializer = new Serializer<>(Psychedelicraft.id(name), codec.codec(), packetCodec);
        CustomIngredientSerializer.register(serializer);
        return serializer;
    }

    static void bootstrap() {}

    record Serializer<T extends CustomIngredient>(Identifier id, Codec<T> codec, PacketCodec<PacketByteBuf, T> packetCodec) implements CustomIngredientSerializer<T> {
        @Override
        public Identifier getIdentifier() {
            return id;
        }

        @Override
        public T read(JsonObject json) {
            return codec.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst).orElseThrow();
        }

        @Override
        public void write(JsonObject json, T ingredient) {
            codec.encodeStart(JsonOps.INSTANCE, ingredient).result().ifPresent(o -> {
                json.asMap().putAll(o.getAsJsonObject().asMap());
            });
        }

        @Override
        public T read(PacketByteBuf buf) {
            return packetCodec.decode(buf);
        }

        @Override
        public void write(PacketByteBuf buf, T ingredient) {
            packetCodec.encode(buf, ingredient);
        }
    }
}
