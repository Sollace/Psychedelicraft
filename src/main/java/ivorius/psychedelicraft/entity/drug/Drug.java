/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import java.util.*;

import ivorius.psychedelicraft.util.NbtSerialisable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
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

    AggregateModifier HEAD_MOTION_INERTNESS = AggregateModifier.create(0, Drug::headMotionInertness, AggregateModifier.Combiner.SUM);
    AggregateModifier VIEW_TREMBLE_STRENGTH = AggregateModifier.create(0, Drug::viewTrembleStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier HAND_TREMBLE_STRENGTH = AggregateModifier.create(0, Drug::handTrembleStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier DOUBLE_VISION = AggregateModifier.create(0, Drug::doubleVision, AggregateModifier.Combiner.INVERSE_MUL);

    AggregateModifier COLOR_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::colorHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier MOVEMENT_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::movementHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier CONTEXTUAL_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::contextualHallucinationStrength, AggregateModifier.Combiner.SUM);

    AggregateModifier SUPER_SATURATION_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::superSaturationHallucinationStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier DESATURATION_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::desaturationHallucinationStrength, AggregateModifier.Combiner.INVERSE_MUL);
    AggregateModifier BLOOM_HALLUCINATION_STRENGTH = AggregateModifier.create(0, Drug::bloomHallucinationStrength, AggregateModifier.Combiner.SUM);
    AggregateModifier MOTION_BLUR = AggregateModifier.create(0, Drug::motionBlur, AggregateModifier.Combiner.INVERSE_MUL);

    DrugType getType();

    void update(LivingEntity entity, DrugProperties drugProperties);

    void reset(LivingEntity entity, DrugProperties drugProperties);

    /**
     * A value from 0 to 0 indicating the strength of this particular effect.
     */
    double getActiveValue();

    void addToDesiredValue(double effect);

    void setDesiredValue(double effect);

    boolean isVisible();

    void setLocked(boolean drugLocked);

    boolean isLocked();

    float heartbeatVolume();

    float heartbeatSpeed();

    float breathVolume();

    float breathSpeed();

    float randomJumpChance();

    float randomPunchChance();

    float digSpeedModifier();

    float speedModifier();

    float soundVolumeModifier();

    Optional<Text> trySleep(BlockPos pos);

    void applyContrastColorization(float[] rgba);

    void applyColorBloom(float[] rgba);

    float desaturationHallucinationStrength();

    float superSaturationHallucinationStrength();

    float contextualHallucinationStrength();

    float colorHallucinationStrength();

    float movementHallucinationStrength();

    float handTrembleStrength();

    float viewTrembleStrength();

    float headMotionInertness();

    float bloomHallucinationStrength();

    float viewWobblyness();

    float doubleVision();

    float motionBlur();

    @Environment(EnvType.CLIENT)
    void drawOverlays(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);

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

        interface Combiner {
            Combiner MUL = (a, b) -> a * b;
            Combiner SUM = (a, b) -> a + b;
            Combiner INVERSE_MUL = (a, b) -> a + (1 - a) * b;

            float combine(float a, float b);
        }
    }
}
