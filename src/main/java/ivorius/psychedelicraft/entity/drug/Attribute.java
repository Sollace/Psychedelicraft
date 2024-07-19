package ivorius.psychedelicraft.entity.drug;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Vector4f;

public record Attribute(String name, float initial, float fallback, Combiner combiner) implements DrugAttributeFunctions.Func {
    public Attribute(String name, float initial, Combiner combiner) {
        this(name, initial, initial, combiner);
    }

    public float get(DrugProperties properties) {
        return get(initial, properties);
    }

    public float get(float value, DrugProperties properties) {
        for (Drug drug : properties.getAllDrugs()) {
            var func = drug.getType().functions().get(this);
            value = combiner.combine(value, func.apply((float)drug.getActiveValue(), drug.getTicksActive()));
        }
        return value;
    }

    public static Function<DrugProperties, Vector4f> createColorModification(BiConsumer<Drug, Vector4f> modifier) {
        final Vector4f initial = new Vector4f(1, 1, 1, 0);
        return properties -> {
            initial.set(1, 1, 1, 0);
            for (Drug drug : properties.getAllDrugs()) {
                modifier.accept(drug, initial);
            }
            return initial;
        };
    }

    @Override
    public float apply(float strength, int duration) {
        return fallback;
    }

    public interface Combiner {
        Combiner MUL = (a, b) -> a * b;
        Combiner SUM = (a, b) -> a + b;
        Combiner INVERSE_MUL = (a, b) -> a + (1 - a) * b;

        float combine(float a, float b);
    }
}