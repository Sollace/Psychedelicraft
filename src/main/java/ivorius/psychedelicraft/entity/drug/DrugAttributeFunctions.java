package ivorius.psychedelicraft.entity.drug;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

public interface DrugAttributeFunctions {
    Func get(Attribute attribute);

    public interface Func {
        float apply(float strength, int duration);
    }

    static Builder builder() {
        return new Builder();
    }

    static DrugAttributeFunctions empty() {
        return attribute -> Builder.DEFAULT_FUNC;
    }

    public static class Builder {
        static final Func DEFAULT_FUNC = (strength, time) -> 0F;
        private final Map<Attribute, Func> functions = new HashMap<>();

        private Builder() {}

        public Builder put(Attribute attribute, Float2FloatFunction function) {
            return put(attribute, (strength, ticks) -> function.get(strength));
        }

        public Builder put(Attribute attribute, float scale) {
            return put(attribute, (strength, ticks) -> strength * scale);
        }

        public Builder put(Attribute attribute, Func function) {
            functions.put(attribute, function);
            return this;
        }

        public Builder add(Attribute attribute, Function<Func, Func> function) {
            functions.put(attribute, function.apply(get(attribute)));
            return this;
        }

        private Func get(Attribute attribute) {
            return functions.getOrDefault(attribute, DEFAULT_FUNC);
        }

        public DrugAttributeFunctions build() {
            return new HashMap<>(functions)::get;
        }
    }
}
