package ivorius.psychedelicraft.util;

import java.io.IOException;

import com.google.gson.internal.Streams;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public class CodecTypeAdapter<T> extends TypeAdapter<T> {
    private final Codec<T> codec;

    public CodecTypeAdapter(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        Streams.write(codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow(false, s -> {}), out);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return codec.decode(JsonOps.INSTANCE, Streams.parse(in)).getOrThrow(false, s -> {}).getFirst();
    }
}
