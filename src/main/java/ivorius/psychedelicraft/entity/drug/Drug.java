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
    Attribute SPEED = new Attribute("movement_speed", 1, Combiner.MUL);
    Attribute DIG_SPEED = new Attribute("mining_speed", 1, Combiner.MUL);
    Attribute SOUND_VOLUME = new Attribute("sound_volume", 1, Combiner.MUL);
    Attribute BREATH_VOLUME = new Attribute("breath_volume", 0, Combiner.SUM);
    Attribute BREATH_SPEED = new Attribute("breath_speed", 1, 0, Combiner.SUM);
    Attribute HEART_BEAT_VOLUME = new Attribute("pulse_volume", 0, Combiner.SUM);
    Attribute HEART_BEAT_SPEED = new Attribute("pulse_speed", 1, 0, Combiner.SUM);
    Attribute JUMP_CHANCE = new Attribute("jump_chance", 0, Combiner.SUM);
    Attribute PUNCH_CHANCE = new Attribute("punch_chance", 0, Combiner.SUM);

    Attribute WEIGHTLESSNESS = new Attribute("weightlessness", 0, Combiner.SUM);
    Attribute HUNGER_SUPPRESSION = new Attribute("hunger_suppression", 0, Combiner.SUM);
    Attribute HEAD_MOTION_INERTNESS = new Attribute("head_motion_inertness", 0, Combiner.SUM);
    Attribute VIEW_TREMBLE_STRENGTH = new Attribute("view_tremble_strength", 0, Combiner.INVERSE_MUL);
    Attribute VIEW_WOBBLYNESS = new Attribute("view_wobblyness", 0, Combiner.SUM);
    Attribute DROWSYNESS = new Attribute("drowsyness", 0, Combiner.SUM);
    Attribute HAND_TREMBLE_STRENGTH = new Attribute("hand_tremble_strength", 0, Combiner.INVERSE_MUL);
    Attribute DOUBLE_VISION = new Attribute("double_vision", 0, Combiner.INVERSE_MUL);

    Attribute COLOR_HALLUCINATION_STRENGTH = new Attribute("color_hallucination", 0, Combiner.SUM);
    Attribute MOVEMENT_HALLUCINATION_STRENGTH = new Attribute("movement_hallucination", 0, Combiner.SUM);
    Attribute CONTEXTUAL_HALLUCINATION_STRENGTH = new Attribute("contextual_hallucination", 0, Combiner.SUM);

    Attribute SUPER_SATURATION_HALLUCINATION_STRENGTH = new Attribute("super_saturation", 0, Combiner.INVERSE_MUL);
    Attribute DESATURATION_HALLUCINATION_STRENGTH = new Attribute("desaturation", 0, Combiner.INVERSE_MUL);
    Attribute INVERSION_HALLUCINATION_STRENGTH = new Attribute("color_inversion", 0, Combiner.SUM);
    Attribute BLOOM_HALLUCINATION_STRENGTH = new Attribute("bloom_hallucination_strength", 0, Combiner.SUM);
    Attribute MOTION_BLUR = new Attribute("motion_blur", 0, Combiner.INVERSE_MUL);

    Attribute SLOW_COLOR_ROTATION = new Attribute("slow_color_rotation", 0, Combiner.SUM);
    Attribute FAST_COLOR_ROTATION = new Attribute("fast_color_rotation", 0, Combiner.SUM);

    Attribute DISTANT_WAVES = new Attribute("distant_waves", 0, Combiner.SUM);
    Attribute BIG_WAVES = new Attribute("big_waves", 0, Combiner.SUM);
    Attribute SMALL_WAVES = new Attribute("small_waves", 0, Combiner.SUM);
    Attribute WIGGLE_WAVES = new Attribute("wiggle_waves", 0, Combiner.SUM);
    Attribute BUBBLING_WAVES = new Attribute("bubbling_waves", 0, Combiner.SUM);
    Attribute SHATTERING_WAVES = new Attribute("shattering_waves", 0, Combiner.SUM);
    Attribute FRACTALS = new Attribute("fractals", 0, Combiner.SUM);

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

    default void applyContrastColorization(Vector4f rgba) {

    }

    default void applyColorBloom(Vector4f rgba) {

    }
}
