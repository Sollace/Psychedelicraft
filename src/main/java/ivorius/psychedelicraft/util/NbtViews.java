package ivorius.psychedelicraft.util;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

/**
 * Bridges vanilla's {@link ReadView}/{@link WriteView} with the mod's {@link NbtCompound} based serialisation
 * so that saved data keeps the same (flat) layout it had before 1.21.6.
 */
public interface NbtViews {
    MapCodec<NbtCompound> FLAT_COMPOUND = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<NbtCompound> decode(DynamicOps<T> ops, MapLike<T> input) {
            NbtCompound compound = new NbtCompound();
            input.entries().forEach(pair -> ops.getStringValue(pair.getFirst()).ifSuccess(key -> {
                compound.put(key, ops.convertTo(NbtOps.INSTANCE, pair.getSecond()));
            }));
            return DataResult.success(compound);
        }

        @Override
        public <T> RecordBuilder<T> encode(NbtCompound input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            for (String key : input.getKeys()) {
                prefix.add(key, NbtOps.INSTANCE.convertTo(ops, input.get(key)));
            }
            return prefix;
        }
    };

    static NbtCompound read(ReadView view) {
        return view.read(FLAT_COMPOUND).orElseGet(NbtCompound::new);
    }

    static void write(WriteView view, NbtCompound compound) {
        view.put(FLAT_COMPOUND, compound);
    }

    static WrapperLookup lookup(@Nullable World world) {
        return world == null ? DynamicRegistryManager.EMPTY : world.getRegistryManager();
    }
}
