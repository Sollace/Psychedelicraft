/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class CocaineDrug extends SimpleDrug {
    static final Optional<Text> SLEEP_STATUS = Optional.of(Text.translatable("psychedelicraft.sleep.fail.coccaine"));
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(HEART_BEAT_VOLUME, (f, t) -> MathUtils.inverseLerp(f, 0.4F, 1) + (t * 0.0001F) * 1.2F)
            .put(HEART_BEAT_SPEED, (f, t) -> f * 0.1F + (t * 0.0001F))
            .put(BREATH_VOLUME, f -> MathUtils.inverseLerp(f, 0.4f, 1.0f) * 1.5F)
            .put(BREATH_SPEED, 0.8F)
            .put(JUMP_CHANCE, f -> MathUtils.inverseLerp(f, 0.6F, 1) * 0.03F)
            .put(PUNCH_CHANCE, f -> MathUtils.inverseLerp(f, 0.5F, 1) * 0.02F)
            .put(SPEED, f -> 1.0F + f * 0.15F)
            .put(DIG_SPEED, f -> 1.0F + f * 0.15F)
            .put(DESATURATION_HALLUCINATION_STRENGTH, 0.75F)
            .put(HAND_TREMBLE_STRENGTH, f -> MathUtils.inverseLerp(f, 0.6F, 1))
            .put(VIEW_TREMBLE_STRENGTH, f -> MathUtils.inverseLerp(f, 0.8F, 1))
            .put(HEAD_MOTION_INERTNESS, 10)
            .put(BLOOM_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f, 0, 0.6F) * 1.5F)
            .put(COLOR_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.7F, 1) * 0.05F)
            .put(MOVEMENT_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.7F, 1) * 0.05F)
            .put(CONTEXTUAL_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.7F, 1) * 0.05F)
            .build();

    public CocaineDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.COCAINE, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getWorld().random;
            if (!entity.getWorld().isClient) {
                double chance = (getActiveValue() - 0.8F) * 0.1F;

                if (entity.age % 20 == 0 && random.nextFloat() < chance) {
                    entity.damage(drugProperties.damageOf(random.nextFloat() < 0.4F
                            ? PSDamageTypes.STROKE
                            : random.nextFloat() < 0.5F
                            ? PSDamageTypes.HEART_FAILURE
                            : PSDamageTypes.RESPIRATORY_FAILURE), Integer.MAX_VALUE);
                }
            }
        }
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return getActiveValue() > 0.4
                ? SLEEP_STATUS
                : Optional.empty();
    }
}
