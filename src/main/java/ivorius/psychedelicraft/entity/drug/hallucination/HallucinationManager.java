package ivorius.psychedelicraft.entity.drug.hallucination;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 * Updated by Sollace on 14 Jan 2023
 */
public class HallucinationManager {
    private final Vector3f currentMindColor = new Vector3f(1, 1, 1);
    private final Vector4f pulseColor = new Vector4f();

    private final HallucinationTypes hallucinationTypes = new HallucinationTypes();

    private final IntList activeHallucinations = new IntArrayList();
    private final Int2FloatMap hallucinationStrengths = new Int2FloatOpenHashMap();

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

        float totalHallucinationValue = hallucinationTypes.update(properties);
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

        for (int hKey : HallucinationTypes.ALL) {
            hallucinationStrengths.compute(hKey, (k, val) -> MathUtils.nearValue(val == null ? 0 : val, getDesiredValue(random, k), 0.002f, 0.002f));
        }

        MathUtils.apply(currentMindColor, component -> MathUtils.nearValue(component, MathUtils.randomColor(random, properties.getAge(), 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f));
    }

    private float getDesiredValue(Random random, int key) {
        return activeHallucinations.contains(key)
                ? MathUtils.randomColor(random, properties.getAge(), hallucinationTypes.getMultiplier(key), 0.5f, 0.00121f, 0.0019318f)
                : 0;
    }

    private void removeRandomHallucination(Random random) {
        activeHallucinations.removeInt(random.nextInt(activeHallucinations.size()));
    }

    private boolean addRandomHallucination(Random random) {
        float maxValue = 0;
        int currentHallucination = -1;

        for (int hKey : HallucinationTypes.ALL) {
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

    private float getMultiplier(int hallucination) {
        return hallucinationTypes.getMultiplier(hallucination) * hallucinationStrengths.get(hallucination);
    }

    public float getDesaturation() {
        return Drug.DESATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.DESATURATION), properties);
    }

    public float getColorIntensification() {
        return Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.SUPER_SATURATION), properties);
    }

    public float getColorInversion() {
        return Drug.INVERSION_HALLUCINATION_STRENGTH.get(properties);
    }

    public float getBloom() {
        return Drug.BLOOM_HALLUCINATION_STRENGTH.get(getMultiplier(HallucinationTypes.BLOOM), properties);
    }

    public float getEntityHallucinationStrength() {
        return getMultiplier(HallucinationTypes.ENTITIES) * 0.4F;
    }

    public float getSlowColorRotation() {
        return Drug.SLOW_COLOR_ROTATION.get(getMultiplier(HallucinationTypes.SLOW_COLOR_ROTATION), properties);
    }

    public float getQuickColorRotation() {
        return Drug.FAST_COLOR_ROTATION.get(getMultiplier(HallucinationTypes.QUICK_COLOR_ROTATION), properties);
    }

    public float getBigWaveStrength() {
        return Drug.BIG_WAVES.get(getMultiplier(HallucinationTypes.BIG_WAVES) * 0.6F, properties);
    }

    public float getSmallWaveStrength() {
        return Drug.SMALL_WAVES.get(getMultiplier(HallucinationTypes.SMALL_WAVES) * 0.5F, properties);
    }

    public float getWiggleWaveStrength() {
        return Drug.WIGGLE_WAVES.get(getMultiplier(HallucinationTypes.WIGGLE_WAVES) * 0.7F, properties);
    }

    public float getSurfaceFractalStrength() {
        return getMultiplier(HallucinationTypes.SURFACE_FRACTALS);
    }

    public float getSurfaceBubblingStrength() {
        return 0F;
    }

    public float getDistantWorldDeformationStrength() {
        return getMultiplier(HallucinationTypes.DISTANT_WORLD_DEFORMATION);
    }

    public float getSurfaceShatteringStrength() {
        return properties.getDrugValue(DrugType.LSD) * 0.06F;
    }

    public Vector4f getPulseColor() {
        return pulseColor.set(
                currentMindColor,
                RenderPhase.current() == RenderPhase.SKY ? 0F : MathHelper.clamp(getMultiplier(HallucinationTypes.PULSES), 0, 1)
        );
    }

    public Vector4fc getColorBloom() {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.BLOOM.apply(properties),
                MathHelper.clamp(1.5f * getMultiplier(HallucinationTypes.COLOR_BLOOM), 0, 1), false);
    }

    public Vector4fc getContrastColorization() {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.CONTRAST_COLORIZATION.apply(properties),
                MathHelper.clamp(getMultiplier(HallucinationTypes.COLOR_CONTRAST), 0, 1),
                true
        );
    }

    public float[] getBlur() {
        float menuBlur = Math.max(0, ShaderContext.drug(DrugType.SLEEP_DEPRIVATION) - 0.7F) * ShaderContext.tickDelta() * 15;
        float vBlur = ShaderContext.drug(DrugType.POWER) + menuBlur;
        float hBlur = menuBlur + (
              ShaderContext.drug(DrugType.BATH_SALTS) * 6F
            + ShaderContext.drug(DrugType.BATH_SALTS) * (ShaderContext.ticks() % 5)
        );
        return new float[] { hBlur, vBlur };
    }
}
