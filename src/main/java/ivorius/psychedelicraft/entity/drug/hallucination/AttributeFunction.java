package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashMap;
import java.util.Map;

import ivorius.psychedelicraft.entity.drug.Attribute;
import ivorius.psychedelicraft.entity.drug.Drug;

public interface AttributeFunction {
    AttributeFunction DEFAULT = (a, m) -> a.get(m.getProperties());
    AttributeFunction FUNCTIONS = new AttributeFunction.Impl(new HashMap<>())
            .add(Drug.BLOOM_HALLUCINATION_STRENGTH, HallucinationTypes.BLOOM)
            .add(Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH, HallucinationTypes.SUPER_SATURATION)
            .add(Drug.DESATURATION_HALLUCINATION_STRENGTH, HallucinationTypes.DESATURATION)
            .add(Drug.FAST_COLOR_ROTATION, HallucinationTypes.QUICK_COLOR_ROTATION)
            .add(Drug.SLOW_COLOR_ROTATION, HallucinationTypes.SLOW_COLOR_ROTATION)
            .add(Drug.DISTANT_WAVES, HallucinationTypes.DISTANT_WORLD_DEFORMATION)
            .add(Drug.WIGGLE_WAVES, HallucinationTypes.WIGGLE_WAVES, 0.7F)
            .add(Drug.SMALL_WAVES, HallucinationTypes.SMALL_WAVES, 0.5F)
            .add(Drug.BIG_WAVES, HallucinationTypes.BIG_WAVES, 0.6F)
            .add(Drug.FRACTALS, HallucinationTypes.SURFACE_FRACTALS);

    float get(Attribute attribute, HallucinationManager manager);

    record Impl(Map<Attribute, AttributeFunction> functions) implements AttributeFunction {
        public Impl add(Attribute attribute, AttributeFunction function) {
            functions.put(attribute, function);
            return this;
        }

        public Impl add(Attribute attribute, int hallucinationType) {
            return add(attribute, hallucinationType, 1);
        }

        public Impl add(Attribute attribute, int hallucinationType, float multiplier) {
            return add(attribute, (p, manager) -> p.get(manager.getVisualisations().getMultiplier(hallucinationType) * multiplier, manager.getProperties()));
        }

        @Override
        public float get(Attribute attribute, HallucinationManager manager) {
            return functions.getOrDefault(attribute, AttributeFunction.DEFAULT).get(attribute, manager);
        }
    }
}
