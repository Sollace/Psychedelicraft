package ivorius.psychedelicraft.config;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.util.Util;

public record FeatureCustomConfig(boolean enabled, InclusionFilter spawnableBiomes) {
    static final FeatureCustomConfig DEFAULT = new FeatureCustomConfig(true, new InclusionFilter(List.of(), List.of()));

    public record InclusionFilter (List<String> included, List<String> excluded, Function<Predicate<BiomeSelectionContext>, Predicate<BiomeSelectionContext>> filterCache) {
        public static final Codec<FeatureCustomConfig.InclusionFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.listOf().fieldOf("included").forGetter(InclusionFilter::included),
                Codec.STRING.listOf().fieldOf("excluded").forGetter(InclusionFilter::excluded)
        ).apply(i, FeatureCustomConfig.InclusionFilter::new));

        public InclusionFilter(List<String> included, List<String> excluded) {
            this(included, excluded, Util.memoize(inherentPredicate -> BiomeSelector.compile(included, excluded, inherentPredicate)));
        }

        public Predicate<BiomeSelectionContext> createPredicate(Predicate<BiomeSelectionContext> inherentPredicate) {
            return filterCache.apply(inherentPredicate);
        }
    }
}