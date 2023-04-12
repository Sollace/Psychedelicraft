/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import java.util.*;
import java.util.function.*;

import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public interface Drug extends NbtSerialisable {
    AggregateModifier SPEED = AggregateModifier.create(1, Drug::speedModifier, AggregateModifier.Combiner.MUL);
    AggregateModifier DIG_SPEED = AggregateModifier.create(1, Drug::digSpeedModifier, AggregateModifier.Combiner.MUL);
    AggregateModifier SOUND_VOLUME = AggregateModifier.create(1, Drug::soundVolumeModifier, AggregateModifier.Combiner.MUL);
    AggregateModifier BREATH_VOLUME = AggregateModifier.create(0, Drug::breathVolume, AggregateModifier.Combiner.SUM);
    AggregateModifier BREATH_SPEED = AggregateModifier.create(1, Drug::breathSpeed, AggregateModifier.Combiner.SUM);
    AggregateModifier HEART_BEAT_VOLUME = AggregateModifier.create(0, Drug::heartbeatVolume, AggregateModifier.Combiner.SUM);
    AggregateModifier HEART_BEAT_SPEED = AggregateModifier.create(1, Drug::heartbeatSpeed, AggregateModifier.Combiner.SUM);
    AggregateModifier JUMP_CHANCE = AggregateModifier.create(0, Drug::randomJumpChance, AggregateModifier.Combiner.SUM);
    AggregateModifier PUNCH_CHANCE = AggregateModifier.create(0, Drug::randomPunchChance, AggregateModifier.Combiner.SUM);

    AggregateModifier WEIGHTLESSNESS = AggregateModifier.create(0, Drug::weightlessness, AggregateModifier.Combiner.SUM);
    AggregateModifier HEAD_MOTION_INERTNESS = AggregateModifier.create(0, Drug::headMotionInertness, AggregateModifier.Combiner.SUM);
    AggregateModifier VIEW_TREMBLE_STRENGTH = AggregateModifier.create(0, Drug::viewTrembleStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier VIEW_WOBBLYNESS = AggregateModifier.create(0, Drug::viewWobblyness, AggregateModifier.Combiner.SUM);
    AggregateModifier DROWSYNESS = AggregateModifier.create(0, Drug::drowsyness, AggregateModifier.Combiner.SUM);
    AggregateModifier HAND_TREMBLE_STRENGTH = AggregateModifier.create(0, Drug::handTrembleStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier DOUBLE_VISION = AggregateModifier.create(0, Drug::doubleVision, AggregateModifier.Combiner.INVERSE_MUL);

    AggregateModifier COLOR_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::colorHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier MOVEMENT_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::movementHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier CONTEXTUAL_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::contextualHallucinationStrength, AggregateModifier.Combiner.SUM);

    AggregateModifier SUPER_SATURATION_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::superSaturationHallucinationStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier DESATURATION_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::desaturationHallucinationStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier INVERSION_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::colorInversionHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier BLOOM_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::bloomHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier MOTION_BLUR = AggregateModifier.create(0, Drug::motionBlur, AggregateModifier.Combiner.INVERSE_MUL);

    Function<DrugProperties, float[]> CONTRAST_COLORIZATION = AggregateModifier.createColorModification(Drug::applyContrastColorization);
    Function<DrugProperties, float[]> BLOOM = AggregateModifier.createColorModification(Drug::applyColorBloom);

    DrugType getType();

    void update(DrugProperties properties);

    void reset(DrugProperties properties);

    void onWakeUp(DrugProperties properties);

    /**
     * A value from 0 to 1 indicating the strength of this particular effect.
     */
    double getActiveValue();

    void addToDesiredValue(double effect);

    void setDesiredValue(double effect);

    boolean isVisible();

    void setLocked(boolean drugLocked);

    boolean isLocked();


    Optional<Text> trySleep(BlockPos pos);

    void applyContrastColorization(float[] rgba);

    void applyColorBloom(float[] rgba);

    default float heartbeatVolume() {
        return 0;
    }

    default float heartbeatSpeed() {
        return 0;
    }

    default float breathVolume() {
        return 0;
    }

    default float breathSpeed() {
        return 0;
    }

    default float randomJumpChance() {
        return 0;
    }

    default float randomPunchChance() {
        return 0;
    }

    default float digSpeedModifier() {
        return 1;
    }

    default float speedModifier() {
        return 1;
    }

    default float soundVolumeModifier() {
        return 1;
    }

    default float desaturationHallucinationStrength() {
        return 0;
    }

    default float superSaturationHallucinationStrength() {
        return 0;
    }

    default float colorInversionHallucinationStrength() {
        return 0;
    }

    default float contextualHallucinationStrength() {
        return 0;
    }

    default float colorHallucinationStrength() {
        return 0;
    }

    default float movementHallucinationStrength() {
        return 0;
    }

    default float handTrembleStrength() {
        return 0;
    }

    default float viewTrembleStrength() {
        return 0;
    }

    default float headMotionInertness() {
        return 0;
    }

    default float bloomHallucinationStrength() {
        return 0;
    }

    default float viewWobblyness() {
        return 0;
    }

    default float drowsyness() {
        // TODO: Implement comas/longer sleeping times
        return 0;
    }

    default float doubleVision() {
        return 0;
    }

    default float motionBlur() {
        return 0;
    }

    default float weightlessness() {
        return 0;
    }

    public interface Modifier {
        float get(Drug drug);
    }

    public interface AggregateModifier {
        Set<AggregateModifier> MODIFIERS = new HashSet<>();

        float get(DrugProperties properties);

        float get(float initial, DrugProperties properties);

        static AggregateModifier create(float initial, Modifier modifier, Combiner combiner) {
            AggregateModifier result = new AggregateModifier() {
                @Override
                public float get(DrugProperties properties) {
                    return get(initial, properties);
                }

                @Override
                public float get(float value, DrugProperties properties) {
                    for (Drug drug : properties.getAllDrugs()) {
                        value = combiner.combine(value, modifier.get(drug));
                    }
                    return value;
                }
            };
            MODIFIERS.add(result);
            return result;
        }

        static Function<DrugProperties, float[]> createColorModification(BiConsumer<Drug, float[]> modifier) {
            return properties -> {
                float[] initial = {1, 1, 1, 0};
                for (Drug drug : properties.getAllDrugs()) {
                    modifier.accept(drug, initial);
                }
                return initial;
            };
        }

        interface Combiner {
            Combiner MUL = (a, b) -> a * b;
            Combiner SUM = (a, b) -> a + b;
            Combiner INVERSE_MUL = (a, b) -> a + (1 - a) * b;

            float combine(float a, float b);
        }
    }
}
