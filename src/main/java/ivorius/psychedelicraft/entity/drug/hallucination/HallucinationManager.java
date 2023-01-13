package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 */
public class HallucinationManager {
    public static final int HALLUCATION_ENTITIES = 0;
    public static final int HALLUCATION_DESATURATION = 1;
    public static final int HALLUCATION_SUPER_SATURATION = 2;
    public static final int HALLUCATION_SLOW_COLOR_ROTATION = 3;
    public static final int HALLUCATION_QUICK_COLOR_ROTATION = 4;
    public static final int HALLUCATION_BIG_WAVES = 5;
    public static final int HALLUCATION_SMALL_WAVES = 6;
    public static final int HALLUCATION_WIGGLE_WAVES = 7;
    public static final int HALLUCATION_PULSES = 8;
    public static final int HALLUCATION_SURFACE_FRACTALS = 9;
    public static final int HALLUCATION_DISTANT_WORLD_DEFORMATION = 10;
    public static final int HALLUCATION_BLOOM = 11;
    public static final int HALLUCATION_COLOR_BLOOM = 12;
    public static final int HALLUCATION_COLOR_CONTRAST = 13;

    private static final List<Integer> COLOR_HALLUCINATIONS = List.of(HALLUCATION_DESATURATION, HALLUCATION_SUPER_SATURATION, HALLUCATION_SLOW_COLOR_ROTATION, HALLUCATION_QUICK_COLOR_ROTATION, HALLUCATION_PULSES, HALLUCATION_SURFACE_FRACTALS, HALLUCATION_BLOOM, HALLUCATION_COLOR_BLOOM, HALLUCATION_COLOR_CONTRAST);
    private static final List<Integer> MOVEMENT_HALLUCINATIONS = List.of(HALLUCATION_BIG_WAVES, HALLUCATION_SMALL_WAVES, HALLUCATION_WIGGLE_WAVES, HALLUCATION_DISTANT_WORLD_DEFORMATION);
    private static final List<Integer> CONTEXTUAL_HALLUCINATIONS = List.of(HALLUCATION_ENTITIES);
    private static final List<Integer> ALL_HALLUCINATIONS = Stream.of(COLOR_HALLUCINATIONS, MOVEMENT_HALLUCINATIONS, CONTEXTUAL_HALLUCINATIONS).flatMap(List::stream).toList();

    protected final float[] currentMindColor = new float[]{1.0f, 1.0f, 1.0f};

    private final List<DrugHallucination> entities = new ArrayList<>();

    private final List<HallucinationType> hallucinationTypes = List.of(
            new HallucinationType(COLOR_HALLUCINATIONS, Drug.COLOR_HALLUCINATION_STRENGTH),
            new HallucinationType(MOVEMENT_HALLUCINATIONS, Drug.MOVEMENT_HALLUCINATION_STRENGTH),
            new HallucinationType(CONTEXTUAL_HALLUCINATIONS, Drug.CONTEXTUAL_HALLUCINATION_STRENGTH)
    );
    private final List<Integer> activeHallucinations = new ArrayList<>();
    private final Map<Integer, Float> hallucinationValues = ALL_HALLUCINATIONS.stream().collect(Collectors.toMap(Function.identity(), i -> 0F));

    public List<DrugHallucination> getHallucinations() {
        return entities;
    }

    public void update(LivingEntity entity, DrugProperties drugProperties) {
        Random random = entity.getRandom();

        updateEntities(entity, drugProperties, random);

        float totalHallucinationValue = 0;
        for (HallucinationType type : hallucinationTypes) {
            float desiredValue = type.getDesiredValue(drugProperties);
            type.currentValue = MathUtils.nearValue(type.currentValue, desiredValue, 0.01F, 0.01F);
            totalHallucinationValue += type.currentValue;
        }

        int desiredHallucinations = Math.max(0, MathHelper.floor(totalHallucinationValue * 4F + 0.9f));

        if (activeHallucinations.size() > 0) {
            while (random.nextFloat() < 1f / (20 * 60 * 5 / activeHallucinations.size())) {
                removeRandomHallucination(random);
                addRandomHallucination(random);
            }
        }

        while (activeHallucinations.size() > desiredHallucinations) {
            removeRandomHallucination(random);
        }

        while (activeHallucinations.size() < desiredHallucinations) {
            if (!addRandomHallucination(random)) {
                break;
            }
        }

        for (Integer hKey : hallucinationValues.keySet()) {
            float val = hallucinationValues.get(hKey);

            if (activeHallucinations.contains(hKey)) {
                float desiredValue = MathUtils.randomColor(random, drugProperties.ticksExisted, getHallucinationMultiplier(hKey), 0.5f, 0.00121f, 0.0019318f);

                val = MathUtils.nearValue(val, desiredValue, 0.002f, 0.002f);
                hallucinationValues.put(hKey, val);
            } else {
                val = MathUtils.nearValue(val, 0.0f, 0.002f, 0.002f);
                hallucinationValues.put(hKey, val);
            }
        }

        currentMindColor[0] = MathUtils.nearValue(currentMindColor[0], MathUtils.randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f);
        currentMindColor[1] = MathUtils.nearValue(currentMindColor[1], MathUtils.randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0011239f, 0.0019321f), 0.002f, 0.002f);
        currentMindColor[2] = MathUtils.nearValue(currentMindColor[2], MathUtils.randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0011541f, 0.0018682f), 0.002f, 0.002f);
    }

    public void updateEntities(LivingEntity entity, DrugProperties drugProperties, Random random) {
        float hallucinationChance = getHallucinationStrength(drugProperties, 1.0f) * 0.05f;
        if (hallucinationChance > 0.0f) {
            if (random.nextInt((int) (1F / hallucinationChance)) == 0) {
                if (entity instanceof PlayerEntity) {
                    addRandomEntityHallucination((PlayerEntity) entity, drugProperties, random);
                }
            }
        }

        for (Iterator<DrugHallucination> iterator = entities.iterator(); iterator.hasNext(); ) {
            DrugHallucination hallucination = iterator.next();
            hallucination.update();

            if (hallucination.isDead()) {
                iterator.remove();
            }
        }
    }

    public void addRandomEntityHallucination(PlayerEntity player, DrugProperties drugProperties, Random random) {
        if (!player.world.isClient) {
            return;
        }

        if (getNumberOfHallucinations(RastaHeadHallucination.class) == 0 && (random.nextFloat() < 0.1f && drugProperties.getDrugValue(DrugType.CANNABIS) > 0.4f)) {
            entities.add(new RastaHeadHallucination(player));
        } else {
            entities.add(new EntityHallucination(player));
        }
    }

    public int getNumberOfHallucinations(Class<? extends DrugHallucination> aClass) {
        int count = 0;
        for (DrugHallucination hallucination : entities) {
            if (aClass.isAssignableFrom(hallucination.getClass())) {
                count++;
            }
        }

        return count;
    }

    public void receiveChatMessage(LivingEntity entity, String message) {
        for (DrugHallucination h : entities) {
            h.receiveChatMessage(message, entity);
        }
    }

    public void removeRandomHallucination(Random random) {
        activeHallucinations.remove(activeHallucinations.get(random.nextInt(activeHallucinations.size())));
    }

    public boolean addRandomHallucination(Random random) {
        float maxValue = 0.0f;
        int currentHallucination = -1;

        for (int hKey : hallucinationValues.keySet()) {
            if (!activeHallucinations.contains(hKey)) {
                float value = random.nextFloat() * getHallucinationMultiplier(hKey);

                if (value > maxValue) {
                    currentHallucination = hKey;
                    maxValue = value;
                }
            }
        }

        if (currentHallucination >= 0) {
            activeHallucinations.add(currentHallucination);
        }
        return currentHallucination >= 0;
    }

    public float getHallucinationMultiplier(int hallucination) {
        float value = 1;
        for (HallucinationType type : hallucinationTypes) {
            if (type.hallucinations.contains(hallucination)) {
                value *= MathHelper.lerp(type.currentValue, 0, 0.5F);
            }
        }
        return value;
    }

    public float getScaledHallucinationMultiplier(int hallucination) {
        return getHallucinationMultiplier(hallucination) * hallucinationValues.get(hallucination);
    }

    public float getHallucinationStrength(DrugProperties drugProperties, float partialTicks) {
        return 0.4f * getHallucinationMultiplier(HALLUCATION_ENTITIES) * hallucinationValues.get(HALLUCATION_ENTITIES);
    }

    public float getDesaturation(DrugProperties drugProperties, float partialTicks) {
        return Drug.DESATURATION_HALLUCINATION_STRENGTH.get(getScaledHallucinationMultiplier(HALLUCATION_DESATURATION), drugProperties);
    }

    public float getColorIntensification(DrugProperties drugProperties, float partialTicks) {
        return Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH.get(getScaledHallucinationMultiplier(HALLUCATION_SUPER_SATURATION), drugProperties);
    }

    public float getSlowColorRotation(DrugProperties drugProperties, float partialTicks) {
        return getScaledHallucinationMultiplier(HALLUCATION_SLOW_COLOR_ROTATION);
    }

    public float getQuickColorRotation(DrugProperties drugProperties, float partialTicks) {
        return getScaledHallucinationMultiplier(HALLUCATION_QUICK_COLOR_ROTATION);
    }

    public float getBigWaveStrength(DrugProperties drugProperties, float partialTicks) {
        return 0.6f * getScaledHallucinationMultiplier(HALLUCATION_BIG_WAVES);
    }

    public float getSmallWaveStrength(DrugProperties drugProperties, float partialTicks) {
        return 0.5f * getScaledHallucinationMultiplier(HALLUCATION_SMALL_WAVES);
    }

    public float getWiggleWaveStrength(DrugProperties drugProperties, float partialTicks) {
        return 0.7f * getScaledHallucinationMultiplier(HALLUCATION_WIGGLE_WAVES);
    }

    public float getSurfaceFractalStrength(DrugProperties drugProperties, float partialTicks) {
        return getScaledHallucinationMultiplier(HALLUCATION_SURFACE_FRACTALS);
    }

    public float getDistantWorldDeformationStrength(DrugProperties drugProperties, float partialTicks) {
        return getScaledHallucinationMultiplier(HALLUCATION_DISTANT_WORLD_DEFORMATION);
    }

    public void applyPulseColor(DrugProperties drugProperties, float[] pulseColor, float partialTicks) {
        pulseColor[0] = currentMindColor[0];
        pulseColor[1] = currentMindColor[1];
        pulseColor[2] = currentMindColor[2];
        pulseColor[3] = getScaledHallucinationMultiplier(HALLUCATION_PULSES);
    }

    public void applyColorBloom(DrugProperties drugProperties, float[] bloomColor, float partialTicks) {
        for (Drug drug : drugProperties.getAllDrugs()) {
            drug.applyColorBloom(bloomColor);
        }
        MathUtils.mixColorsDynamic(currentMindColor, bloomColor, MathHelper.clamp(1.5f * getScaledHallucinationMultiplier(HALLUCATION_COLOR_BLOOM), 0, 1));
    }

    public void applyContrastColorization(DrugProperties drugProperties, float[] contrastColor, float partialTicks) {
        for (Drug drug : drugProperties.getAllDrugs()) {
            drug.applyContrastColorization(contrastColor);
        }
        MathUtils.mixColorsDynamic(currentMindColor, contrastColor, MathHelper.clamp(getScaledHallucinationMultiplier(HALLUCATION_COLOR_CONTRAST), 0, 1));
    }

    public float getBloom(DrugProperties drugProperties, float partialTicks) {
        return Drug.BLOOM_HALLUCINATION_STRENGTH.get(getScaledHallucinationMultiplier(HALLUCATION_BLOOM), drugProperties);
    }

    public static class HallucinationType {
        public final List<Integer> hallucinations;
        public float currentValue;
        private final Drug.AggregateModifier modifier;

        public HallucinationType(List<Integer> hallucinations, Drug.AggregateModifier modifier) {
            this.hallucinations = new ArrayList<>(hallucinations);
            this.modifier = modifier;
        }

        public float getDesiredValue(DrugProperties drugProperties) {
            return drugProperties.getModifier(modifier);
        }
    }
}
