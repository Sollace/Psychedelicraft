package ivorius.psychedelicraft.datagen.providers.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class SoundTypeBuilder {
    private SoundCategory category = SoundCategory.NEUTRAL;
    private Optional<String> subtitle = Optional.empty();
    private final List<Sound> sounds = new ArrayList<>();

    public static SoundTypeBuilder of(SoundEvent event) {
        return of().subtitle("subtitles." + event.id().getNamespace() + "." + event.id().getPath());
    }

    public static SoundTypeBuilder of() {
        return new SoundTypeBuilder();
    }

    private SoundTypeBuilder() { }

    public SoundTypeBuilder category(SoundCategory category) {
        this.category = category;
        return this;
    }

    public SoundTypeBuilder subtitle(String subtitle) {
        this.subtitle = Optional.of(subtitle);
        return this;
    }

    public SoundTypeBuilder sound(Sound.Builder sound) {
        sounds.add(sound.build(""));
        return this;
    }

    public SoundTypeBuilder sound(Sound.Builder sound, int count) {
        for (int i = 1; i <= count; i++) {
            sounds.add(sound.build("" + i));
        }
        return this;
    }

    public SoundType build() {
        Preconditions.checkState(!sounds.isEmpty(), "Sound definition must have at least one sound file");
        for (Sound sound : sounds) {
            if (sound.type() == Sound.RegistrationType.SOUND_EVENT) {
                Registries.SOUND_EVENT.getOptionalValue(sound.name()).orElseThrow(() -> new IllegalStateException("References sound event " + sound.name() + " does not exist"));
            }
        }

        return new SoundType(sounds, category, subtitle);
    }

    public record SoundType(List<Sound> sounds, SoundCategory category, Optional<String> subtitle) {
        private static final Map<String, SoundCategory> CATEGORIES = Arrays.stream(SoundCategory.values()).collect(Collectors.toMap(SoundCategory::getName, Function.identity()));
        private static final Codec<SoundCategory> SOUND_CATEGORY_CODEC = Codec.stringResolver(SoundCategory::getName, name -> CATEGORIES.getOrDefault(name.toLowerCase(Locale.ROOT), SoundCategory.NEUTRAL));
        public static final Codec<SoundType> CODEC = RecordCodecBuilder.create(i -> i.group(
                Sound.CODEC.listOf().fieldOf("sounds").forGetter(SoundType::sounds),
                SOUND_CATEGORY_CODEC.fieldOf("category").forGetter(SoundType::category),
                Codec.STRING.optionalFieldOf("subtitle").forGetter(SoundType::subtitle)
        ).apply(i, SoundType::new));
    }

    public record Sound(Identifier name, RegistrationType type, float volume, float pitch, int weight, int attenuationDistance, boolean stream, boolean preload) {
        private static final Codec<Sound> MAP_CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("name").forGetter(Sound::name),
                RegistrationType.CODEC.optionalFieldOf("type", RegistrationType.FILE).forGetter(Sound::type),
                Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(Sound::volume),
                Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(Sound::pitch),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(Sound::weight),
                Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(Sound::attenuationDistance),
                Codec.BOOL.optionalFieldOf("stream", false).forGetter(Sound::stream),
                Codec.BOOL.optionalFieldOf("preload", false).forGetter(Sound::preload)
        ).apply(i, Sound::new));

        private static final Codec<Sound> STRING_CODEC = Identifier.CODEC.xmap(
                id -> new Sound(id, RegistrationType.FILE, 1F, 1F, 1, 16, false, false),
                Sound::name
        );
        public static final Codec<Sound> CODEC = Codec.xor(STRING_CODEC, MAP_CODEC).xmap(Either::unwrap, sound -> {
            if (sound.type() != RegistrationType.FILE || sound.volume() != 1F || sound.pitch() != 1F || sound.weight() != 1 || sound.attenuationDistance() != 16 || sound.stream() || sound.preload()) {
                return Either.right(sound);
            }
            return Either.left(sound);
        });

        public static Builder builder(Identifier name) {
            return new Builder(name);
        }

        public enum RegistrationType implements StringIdentifiable {
            FILE("fule"),
            SOUND_EVENT("event");

            public static final Codec<RegistrationType> CODEC = StringIdentifiable.createCodec(RegistrationType::values);

            private final String name;

            RegistrationType(String name) {
                this.name = name;
            }

            @Override
            public String asString() {
                return name;
            }
        }

        public static class Builder {
            private final Identifier name;
            private float volume = 1F;
            private float pitch = 1F;
            private int attenuationDistance = 16;
            private int weight = 1;
            private boolean stream = false;
            private boolean preload = false;
            private RegistrationType type = RegistrationType.FILE;

            private Builder(Identifier name) {
                this.name = name;
            }

            public Builder volume(float volume) {
                this.volume = volume;
                return this;
            }

            public Builder pitch(float pitch) {
                this.pitch = pitch;
                return this;
            }

            public Builder attenuationDistance(int attenuationDistance) {
                this.attenuationDistance = attenuationDistance;
                return this;
            }

            public Builder weight(int weight) {
                this.weight = weight;
                return this;
            }

            public Builder stream(boolean stream) {
                this.stream = stream;
                return this;
            }

            public Builder preload(boolean preload) {
                this.preload = preload;
                return this;
            }

            public Builder type(RegistrationType type) {
                this.type = type;
                return this;
            }

            public Sound build(String suffix) {
                return new Sound(name.withSuffixedPath(suffix), type, volume, pitch, weight, attenuationDistance, stream, preload);
            }
        }
    }

}
