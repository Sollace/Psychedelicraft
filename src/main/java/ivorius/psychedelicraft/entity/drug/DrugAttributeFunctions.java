package ivorius.psychedelicraft.entity.drug;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

public interface DrugAttributeFunctions {
    DrugAttributeFunctions EMPTY = attribute -> attribute;

    Func get(Attribute attribute);

    public interface Func {
        float apply(float strength, int duration);
    }

    static Builder builder() {
        return new Builder();
    }

    static DrugAttributeFunctions empty() {
        return EMPTY;
    }

    public static class Builder {
        private final Impl functions = new Impl(new HashMap<>());

        private Builder() {}

        public Builder put(Attribute attribute, Float2FloatFunction function) {
            return put(attribute, (strength, ticks) -> function.get(strength));
        }

        public Builder put(Attribute attribute, float scale) {
            return put(attribute, (strength, ticks) -> strength * scale);
        }

        public Builder put(Attribute attribute, Func function) {
            functions.functions().put(attribute, function);
            return this;
        }

        public Builder add(Attribute attribute, Function<Func, Func> function) {
            functions.functions().put(attribute, function.apply(functions.get(attribute)));
            return this;
        }

        public DrugAttributeFunctions build() {
            return new Impl(Map.copyOf(functions.functions()));
        }

        private record Impl(Map<Attribute, Func> functions) implements DrugAttributeFunctions {
            @Override
            public Func get(Attribute attribute) {
                return functions.getOrDefault(attribute, attribute);
            }
        }
    }
}
