/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import java.util.*;
import java.util.function.*;

import org.joml.Vector4f;

import ivorius.psychedelicraft.entity.drug.Attribute.Combiner;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public interface Drug extends NbtSerialisable {
    Attribute SPEED = new Attribute(1, Combiner.MUL);
    Attribute DIG_SPEED = new Attribute(1, Combiner.MUL);
    Attribute SOUND_VOLUME = new Attribute(1, Combiner.MUL);
    Attribute BREATH_VOLUME = new Attribute(0, Combiner.SUM);
    Attribute BREATH_SPEED = new Attribute(1, Combiner.SUM);
    Attribute HEART_BEAT_VOLUME = new Attribute(0, Combiner.SUM);
    Attribute HEART_BEAT_SPEED = new Attribute(1, Combiner.SUM);
    Attribute JUMP_CHANCE = new Attribute(0, Combiner.SUM);
    Attribute PUNCH_CHANCE = new Attribute(0, Combiner.SUM);

    Attribute WEIGHTLESSNESS = new Attribute(0, Combiner.SUM);
    Attribute HUNGER_SUPPRESSION = new Attribute(0, Combiner.SUM);
    Attribute HEAD_MOTION_INERTNESS = new Attribute(0, Combiner.SUM);
    Attribute VIEW_TREMBLE_STRENGTH = new Attribute(0, Combiner.INVERSE_MUL);
    Attribute VIEW_WOBBLYNESS = new Attribute(0, Combiner.SUM);
    Attribute DROWSYNESS = new Attribute(0, Combiner.SUM);
    Attribute HAND_TREMBLE_STRENGTH = new Attribute(0, Combiner.INVERSE_MUL);
    Attribute DOUBLE_VISION = new Attribute(0, Combiner.INVERSE_MUL);

    Attribute COLOR_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.SUM);
    Attribute MOVEMENT_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.SUM);
    Attribute CONTEXTUAL_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.SUM);

    Attribute SUPER_SATURATION_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.INVERSE_MUL);
    Attribute DESATURATION_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.INVERSE_MUL);
    Attribute INVERSION_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.SUM);
    Attribute BLOOM_HALLUCINATION_STRENGTH = new Attribute(0, Combiner.SUM);
    Attribute MOTION_BLUR = new Attribute(0, Combiner.INVERSE_MUL);

    Attribute SLOW_COLOR_ROTATION = new Attribute(0, Combiner.SUM);
    Attribute FAST_COLOR_ROTATION = new Attribute(0, Combiner.SUM);

    Attribute BIG_WAVES = new Attribute(0, Combiner.SUM);
    Attribute SMALL_WAVES = new Attribute(0, Combiner.SUM);
    Attribute WIGGLE_WAVES = new Attribute(0, Combiner.SUM);

    Function<DrugProperties, Vector4f> CONTRAST_COLORIZATION = Attribute.createColorModification(Drug::applyContrastColorization);
    Function<DrugProperties, Vector4f> BLOOM = Attribute.createColorModification(Drug::applyColorBloom);

    DrugType getType();

    void update(DrugProperties properties);

    void reset(DrugProperties properties);

    void onWakeUp(DrugProperties properties);

    /**
     * A value from 0 to 1 indicating the strength of this particular effect.
     */
    double getActiveValue();

    int getTicksActive();

    void addToDesiredValue(double effect);

    void setDesiredValue(double effect);

    boolean isVisible();

    void setLocked(boolean drugLocked);

    boolean isLocked();

    Optional<Text> trySleep(BlockPos pos);

    default float get(Attribute attribute) {
        return getType().functions().get(attribute).apply((float)getActiveValue(), getTicksActive());
    }

    default void applyContrastColorization(Vector4f rgba) {

    }

    default void applyColorBloom(Vector4f rgba) {

    }
}
