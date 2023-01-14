package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 */
public class HallucinationManager {
    protected final float[] currentMindColor = new float[]{1.0f, 1.0f, 1.0f};

    private final HallucinationTypes hallucinationTypes = new HallucinationTypes();

    private final List<Integer> activeHallucinations = new ArrayList<>();
    private final Map<Integer, Float> hallucinationStrengths = HallucinationTypes.ALL.stream().collect(Collectors.toMap(Function.identity(), i -> 0F));

    private final DrugProperties properties;
    private final EntityHallucinationList entities = new EntityHallucinationList(this);

    public HallucinationManager(DrugProperties properties) {
        this.properties = properties;
    }

    public DrugProperties getProperties() {
        return properties;
    }

    public EntityHallucinationList getEntities() {
        return entities;
    }

    public void update() {
        entities.update();

        float totalHallucinationValue = hallucinationTypes.getTotal(properties);
        int desiredHallucinations = Math.max(0, MathHelper.floor(totalHallucinationValue * 4F + 0.9f));

        Random random = properties.asEntity().getRandom();

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

        for (Integer hKey : hallucinationStrengths.keySet()) {
            float val = hallucinationStrengths.get(hKey);

            if (activeHallucinations.contains(hKey)) {
                float desiredValue = MathUtils.randomColor(random, properties.age, hallucinationTypes.getMultiplier(hKey), 0.5f, 0.00121f, 0.0019318f);

                val = MathUtils.nearValue(val, desiredValue, 0.002f, 0.002f);
                hallucinationStrengths.put(hKey, val);
            } else {
                val = MathUtils.nearValue(val, 0.0f, 0.002f, 0.002f);
                hallucinationStrengths.put(hKey, val);
            }
        }

        currentMindColor[0] = MathUtils.nearValue(currentMindColor[0], MathUtils.randomColor(random, properties.age, 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f);
        currentMindColor[1] = MathUtils.nearValue(currentMindColor[1], MathUtils.randomColor(random, properties.age, 0.5f, 0.5f, 0.0011239f, 0.0019321f), 0.002f, 0.002f);
        currentMindColor[2] = MathUtils.nearValue(currentMindColor[2], MathUtils.randomColor(random, properties.age, 0.5f, 0.5f, 0.0011541f, 0.0018682f), 0.002f, 0.002f);
    }

    private void removeRandomHallucination(Random random) {
        activeHallucinations.remove(activeHallucinations.get(random.nextInt(activeHallucinations.size())));
    }

    private boolean addRandomHallucination(Random random) {
        float maxValue = 0.0f;
        int currentHallucination = -1;

        for (int hKey : hallucinationStrengths.keySet()) {
            if (!activeHallucinations.contains(hKey)) {
                float value = random.nextFloat() * hallucinationTypes.getMultiplier(hKey);

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

    public float getMultiplier(int hallucination) {
        return hallucinationTypes.getMultiplier(hallucination) * hallucinationStrengths.get(hallucination);
    }

    public float getHallucinationStrength(float partialTicks) {
        return 0.4f * getMultiplier(HallucinationTypes.ENTITIES);
    }

    public float getDesaturation(float partialTicks) {
        return Drug.DESATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.DESATURATION), properties);
    }

    public float getColorIntensification(float partialTicks) {
        return Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.SUPER_SATURATION), properties);
    }

    public float getSlowColorRotation(float partialTicks) {
        return getMultiplier(HallucinationTypes.SLOW_COLOR_ROTATION);
    }

    public float getQuickColorRotation(float partialTicks) {
        return getMultiplier(HallucinationTypes.QUICK_COLOR_ROTATION);
    }

    public float getBigWaveStrength(float partialTicks) {
        return 0.6f * getMultiplier(HallucinationTypes.BIG_WAVES);
    }

    public float getSmallWaveStrength(float partialTicks) {
        return 0.5f * getMultiplier(HallucinationTypes.SMALL_WAVES);
    }

    public float getWiggleWaveStrength(float partialTicks) {
        return 0.7f * getMultiplier(HallucinationTypes.WIGGLE_WAVES);
    }

    public float getSurfaceFractalStrength(float partialTicks) {
        return getMultiplier(HallucinationTypes.SURFACE_FRACTALS);
    }

    public float getDistantWorldDeformationStrength(float partialTicks) {
        return getMultiplier(HallucinationTypes.DISTANT_WORLD_DEFORMATION);
    }

    public float[] getPulseColor(float partialTicks) {
        return new float[] {
                currentMindColor[0],
                currentMindColor[1],
                currentMindColor[2],
                MathHelper.clamp(getMultiplier(HallucinationTypes.PULSES), 0, 1)
        };
    }

    public float[] getColorBloom(float partialTicks) {
        float[] bloomColor = new float[] {1, 1, 1, 0};
        for (Drug drug : properties.getAllDrugs()) {
            drug.applyColorBloom(bloomColor);
        }
        MathUtils.mixColorsDynamic(currentMindColor, bloomColor, MathHelper.clamp(1.5f * getMultiplier(HallucinationTypes.COLOR_BLOOM), 0, 1));
        return bloomColor;
    }

    public float[] getContrastColorization(float partialTicks) {
        float[] contrastColor = new float[] {1, 1, 1, 0};
        for (Drug drug : properties.getAllDrugs()) {
            drug.applyContrastColorization(contrastColor);
        }
        MathUtils.mixColorsDynamic(currentMindColor, contrastColor, MathHelper.clamp(getMultiplier(HallucinationTypes.COLOR_CONTRAST), 0, 1));
        contrastColor[3] = MathHelper.clamp(contrastColor[3], 0, 1);
        return contrastColor;
    }

    public float getBloom(float partialTicks) {
        return Drug.BLOOM_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.BLOOM), properties);
    }
}
