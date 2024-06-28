/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 01.11.14.
 */
public class CaffeineDrug extends SimpleDrug {
    static final Optional<Text> SLEEP_STATUS = Optional.of(Text.translatable("psychedelicraft.sleep.fail.insomnia"));

    public static DrugAttributeFunctions functions(float breathVolumeMultiplier) {
        return DrugAttributeFunctions.builder()
                .put(HEART_BEAT_VOLUME, (f, t) -> breathVolumeMultiplier * MathUtils.project(f, 0.6F, 1) + (t * 0.001F))
                .put(HEART_BEAT_SPEED, (f, t) -> breathVolumeMultiplier * f * 0.2f + (t * 0.001F))
                .put(BREATH_VOLUME, f -> breathVolumeMultiplier * MathUtils.project(f, 0.4F, 1) * 0.5F)
                .put(BREATH_SPEED, f -> breathVolumeMultiplier * f * 0.3F)
                .put(JUMP_CHANCE, f -> MathUtils.project(f, 0.6F, 1) * 0.07F)
                .put(PUNCH_CHANCE, f -> MathUtils.project(f, 0.3F, 1) * 0.05F)
                .put(SPEED, f -> 1 + f * 0.2F)
                .put(DIG_SPEED, f -> 1 + f * 0.2F)
                .put(SUPER_SATURATION_HALLUCINATION_STRENGTH, 0.3F)
                .put(COLOR_HALLUCINATION_STRENGTH, f -> MathUtils.project(f * 1.3F, 0.7F, 1) * 0.03F)
                .put(MOVEMENT_HALLUCINATION_STRENGTH, f -> MathUtils.project(f * 1.3F, 0.7F, 1) * 0.03F)
                .put(CONTEXTUAL_HALLUCINATION_STRENGTH, f -> MathUtils.project(f * 1.3F, 0.7F, 1) * 0.05F)
                .put(HAND_TREMBLE_STRENGTH, f -> MathUtils.project(f, 0.6F, 1))
                .put(VIEW_TREMBLE_STRENGTH, f -> MathUtils.project(f, 0.8F, 1))
                .put(HUNGER_SUPPRESSION, 0.15F)
                .build();
    }

    public CaffeineDrug(DrugType<CaffeineDrug> type, double decSpeed, double decSpeedPlus) {
        super(type, decSpeed, decSpeedPlus);
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return getActiveValue() > 0.1 ? SLEEP_STATUS : Optional.empty();
    }
}
