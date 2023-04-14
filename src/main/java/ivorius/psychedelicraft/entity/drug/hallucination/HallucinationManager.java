package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 * Updated by Sollace on 14 Jan 2023
 */
public class HallucinationManager {
    protected final float[] currentMindColor = new float[]{1.0f, 1.0f, 1.0f};

    private final HallucinationTypes hallucinationTypes = new HallucinationTypes();

    private final List<Integer> activeHallucinations = new ArrayList<>();
    private final Map<Integer, Float> hallucinationStrengths = HallucinationTypes.ALL.stream().collect(Collectors.toMap(Function.identity(), i -> 0F));

    private final DrugProperties properties;
    private final EntityHallucinationList entities = new EntityHallucinationList(this);

    private final DriftingCamera camera = new DriftingCamera();

    public HallucinationManager(DrugProperties properties) {
        this.properties = properties;
    }

    public DrugProperties getProperties() {
        return properties;
    }

    public EntityHallucinationList getEntities() {
        return entities;
    }

    public DriftingCamera getCamera() {
        return camera;
    }

    public void update() {
        entities.update();
        camera.update(properties);

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
                float desiredValue = MathUtils.randomColor(random, properties.getAge(), hallucinationTypes.getMultiplier(hKey), 0.5f, 0.00121f, 0.0019318f);

                val = MathUtils.nearValue(val, desiredValue, 0.002f, 0.002f);
                hallucinationStrengths.put(hKey, val);
            } else {
                val = MathUtils.nearValue(val, 0.0f, 0.002f, 0.002f);
                hallucinationStrengths.put(hKey, val);
            }
        }

        currentMindColor[0] = MathUtils.nearValue(currentMindColor[0], MathUtils.randomColor(random, properties.getAge(), 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f);
        currentMindColor[1] = MathUtils.nearValue(currentMindColor[1], MathUtils.randomColor(random, properties.getAge(), 0.5f, 0.5f, 0.0011239f, 0.0019321f), 0.002f, 0.002f);
        currentMindColor[2] = MathUtils.nearValue(currentMindColor[2], MathUtils.randomColor(random, properties.getAge(), 0.5f, 0.5f, 0.0011541f, 0.0018682f), 0.002f, 0.002f);
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

    public float getDesaturation(float tickDelta) {
        return Drug.DESATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.DESATURATION), properties);
    }

    public float getColorIntensification(float tickDelta) {
        return Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.SUPER_SATURATION), properties);
    }

    public float getBloom(float tickDelta) {
        return Drug.BLOOM_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.BLOOM), properties);
    }

    public float getHallucinationStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.ENTITIES) * 0.4F;
    }

    public float getSlowColorRotation(float tickDelta) {
        return getMultiplier(HallucinationTypes.SLOW_COLOR_ROTATION);
    }

    public float getQuickColorRotation(float tickDelta) {
        return getMultiplier(HallucinationTypes.QUICK_COLOR_ROTATION);
    }

    public float getBigWaveStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.BIG_WAVES) * 0.6F;
    }

    public float getSmallWaveStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.SMALL_WAVES) * 0.5F;
    }

    public float getWiggleWaveStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.WIGGLE_WAVES) * 0.7F;
    }

    public float getSurfaceFractalStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.SURFACE_FRACTALS);
    }

    public float getDistantWorldDeformationStrength(float tickDelta) {
        return getMultiplier(HallucinationTypes.DISTANT_WORLD_DEFORMATION);
    }

    public float getSurfaceShatteringStrength(float tickDelta) {
        return properties.getDrugValue(DrugType.LSD) * 0.06F;
    }

    public float[] getPulseColor(float tickDelta) {
        return new float[] {
                currentMindColor[0],
                currentMindColor[1],
                currentMindColor[2],
                RenderPhase.current() == RenderPhase.SKY ? 0 : MathHelper.clamp(getMultiplier(HallucinationTypes.PULSES), 0, 1)
        };
    }

    public float[] getColorBloom(float tickDelta) {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.BLOOM.apply(properties),
                MathHelper.clamp(1.5f * getMultiplier(HallucinationTypes.COLOR_BLOOM), 0, 1), false);
    }

    public float[] getContrastColorization(float tickDelta) {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.CONTRAST_COLORIZATION.apply(properties),
                MathHelper.clamp(getMultiplier(HallucinationTypes.COLOR_CONTRAST), 0, 1),
                true
        );
    }
}
