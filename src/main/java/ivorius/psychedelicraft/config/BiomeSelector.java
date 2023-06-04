package ivorius.psychedelicraft.config;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface BiomeSelector {
    Predicate<BiomeSelectionContext> ALL = BiomeSelectors.all();
    Predicate<BiomeSelectionContext> NONE = ctx -> false;
    Predicate<BiomeSelectionContext> COLD = ctx -> ctx.getBiome().getTemperature() < 0.15F;
    Predicate<BiomeSelectionContext> DRY = ctx -> !ctx.getBiome().hasPrecipitation();

    static Predicate<BiomeSelectionContext> compile(String[] included, String[] excluded, Predicate<BiomeSelectionContext> dynamicInclusion) {
        var include = compile(included, NONE, Stream::allMatch).or(dynamicInclusion);
        var exclude = compile(excluded, ALL, Stream::noneMatch);

        return include.and(exclude);
    }

    static Predicate<BiomeSelectionContext> compile(String[] predicates, Predicate<BiomeSelectionContext> fallback,
            BiPredicate<Stream<Predicate<BiomeSelectionContext>>, Predicate<Predicate<BiomeSelectionContext>>> combiner) {

        if (predicates == null || predicates.length == 0) {
            return fallback;
        }

        var selectors = Arrays.stream(predicates).map(BiomeSelector::compile).toList();

        if (selectors.size() == 1) {
            return selectors.get(0);
        }
        return ctx -> combiner.test(selectors.stream(), a -> a.test(ctx));
    }

    static Predicate<BiomeSelectionContext> compile(String selector) {
        if (selector.startsWith("#")) {
            return BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, new Identifier(selector.substring(1))));
        }

        return ofId(new Identifier(selector));
    }

    static Predicate<BiomeSelectionContext> ofId(Identifier tagId) {
        return ctx -> ctx.getBiomeRegistryEntry().matchesId(tagId);
    }
}
