package ivorius.psychedelicraft.datagen.providers.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundTypeBuilder {
    private SoundCategory category = SoundCategory.NEUTRAL;
    private Optional<String> subtitle = Optional.empty();
    private final List<Sound> sounds = new ArrayList<>();

    public static SoundTypeBuilder of(SoundEvent event) {
        return of().subtitle("subtitle." + event.getId().getNamespace() + "." + event.getId().getPath());
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

    public SoundTypeBuilder sound(Sound sound) {
        sounds.add(sound);
        return this;
    }

    public SoundTypeBuilder sound(Sound sound, int count) {
        for (int i = 1; i <= count; i++) {
            sound(new Sound(sound.name().withSuffixedPath(i + ""), sound.volume(), sound.pitch(), sound.attenuationDistance(), sound.stream()));
        }
        return this;
    }

    public SoundType build() {
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

    public record Sound(Identifier name, Optional<Float> volume, Optional<Float> pitch, Optional<Integer> attenuationDistance, Optional<Boolean> stream) {
        private static final Codec<Sound> MAP_CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("name").forGetter(Sound::name),
                Codec.FLOAT.optionalFieldOf("volume").forGetter(Sound::volume),
                Codec.FLOAT.optionalFieldOf("pitch").forGetter(Sound::pitch),
                Codec.INT.optionalFieldOf("attenuation_distance").forGetter(Sound::attenuationDistance),
                Codec.BOOL.optionalFieldOf("stream").forGetter(Sound::stream)
        ).apply(i, Sound::new));
        private static final Codec<Sound> STRING_CODEC = Identifier.CODEC.xmap(
                id -> new Sound(id, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                Sound::name
        );
        public static final Codec<Sound> CODEC = Codec.xor(STRING_CODEC, MAP_CODEC).xmap(Either::unwrap, sound -> {
            if (sound.volume().isPresent() || sound.pitch().isPresent() || sound.attenuationDistance().isPresent() || sound.stream().isPresent()) {
                return Either.right(sound);
            }
            return Either.left(sound);
        });

        public static Builder builder(Identifier name) {
            return new Builder(name);
        }

        public static class Builder {
            private final Identifier name;
            private Optional<Float> volume = Optional.empty();
            private Optional<Float> pitch = Optional.empty();
            private Optional<Integer> attenuationDistance = Optional.empty();
            private Optional<Boolean> stream = Optional.empty();

            private Builder(Identifier name) {
                this.name = name;
            }

            public Builder volume(float volume) {
                this.volume = Optional.of(volume);
                return this;
            }

            public Builder pitch(float pitch) {
                this.pitch = Optional.of(pitch);
                return this;
            }

            public Builder attenuationDistance(int attenuationDistance) {
                this.attenuationDistance = Optional.of(attenuationDistance);
                return this;
            }

            public Builder stream(boolean stream) {
                this.stream = Optional.of(stream);
                return this;
            }

            public Sound build() {
                return new Sound(name, volume, pitch, attenuationDistance, stream);
            }
        }
    }

}
