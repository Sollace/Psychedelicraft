package ivorius.psychedelicraft.entities.drugs;

import java.util.*;

import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 */
public class DrugHallucinationManager {
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

    protected final float[] currentMindColor = new float[]{1.0f, 1.0f, 1.0f};

    public static void mixColorsDynamic(float[] color, float[] colorBase, float alpha) {
        if (alpha > 0.0f) {
            float max = alpha + colorBase[3];
            colorBase[0] = MathHelper.lerp(alpha / max, colorBase[0], color[0]);
            colorBase[1] = MathHelper.lerp(alpha / max, colorBase[1], color[1]);
            colorBase[2] = MathHelper.lerp(alpha / max, colorBase[2], color[2]);
            colorBase[3] = max;
        }
    }

    public static float randomColor(Random random, int ticksExisted, float base, float sway, float... speed) {
        for (float s : speed) {
            base *= 1.0f + MathHelper.sin(ticksExisted * s) * sway;
        }
        return base;
    }

    public final List<DrugHallucination> entities = new ArrayList<>();

    public final List<HallucinationType> hallucinationTypes = new ArrayList<>();
    public final List<Integer> activeHallucinations = new ArrayList<>();
    public final Map<Integer, Float> hallucinationValues = new HashMap<>();

    public DrugHallucinationManager()
    {
        HallucinationType colorHallucinationType = new HallucinationType()
        {
            @Override
            public float getDesiredValue(DrugProperties drugProperties)
            {
                float val = 0.0f;
                for (Drug drug : drugProperties.getAllDrugs())
                    val += drug.colorHallucinationStrength();
                return val;
            }
        };
        hallucinationTypes.add(colorHallucinationType);
        Collections.addAll(colorHallucinationType.hallucinations, HALLUCATION_DESATURATION, HALLUCATION_SUPER_SATURATION, HALLUCATION_SLOW_COLOR_ROTATION, HALLUCATION_QUICK_COLOR_ROTATION, HALLUCATION_PULSES, HALLUCATION_SURFACE_FRACTALS, HALLUCATION_BLOOM, HALLUCATION_COLOR_BLOOM, HALLUCATION_COLOR_CONTRAST);

        HallucinationType movementHallucinationType = new HallucinationType()
        {
            @Override
            public float getDesiredValue(DrugProperties drugProperties)
            {
                float val = 0.0f;
                for (Drug drug : drugProperties.getAllDrugs())
                    val += drug.movementHallucinationStrength();
                return val;
            }
        };
        hallucinationTypes.add(movementHallucinationType);
        Collections.addAll(movementHallucinationType.hallucinations, HALLUCATION_BIG_WAVES, HALLUCATION_SMALL_WAVES, HALLUCATION_WIGGLE_WAVES, HALLUCATION_DISTANT_WORLD_DEFORMATION);

        HallucinationType contextualHallucinationType = new HallucinationType()
        {
            @Override
            public float getDesiredValue(DrugProperties drugProperties)
            {
                float val = 0.0f;
                for (Drug drug : drugProperties.getAllDrugs())
                    val += drug.contextualHallucinationStrength();
                return val;
            }
        };
        hallucinationTypes.add(contextualHallucinationType);
        Collections.addAll(contextualHallucinationType.hallucinations, HALLUCATION_ENTITIES);

        hallucinationValues.put(HALLUCATION_ENTITIES, 0.0f);
        hallucinationValues.put(HALLUCATION_DESATURATION, 0.0f);
        hallucinationValues.put(HALLUCATION_SUPER_SATURATION, 0.0f);
        hallucinationValues.put(HALLUCATION_SLOW_COLOR_ROTATION, 0.0f);
        hallucinationValues.put(HALLUCATION_QUICK_COLOR_ROTATION, 0.0f);
        hallucinationValues.put(HALLUCATION_BIG_WAVES, 0.0f);
        hallucinationValues.put(HALLUCATION_SMALL_WAVES, 0.0f);
        hallucinationValues.put(HALLUCATION_WIGGLE_WAVES, 0.0f);
        hallucinationValues.put(HALLUCATION_PULSES, 0.0f);
        hallucinationValues.put(HALLUCATION_SURFACE_FRACTALS, 0.0f);
        hallucinationValues.put(HALLUCATION_DISTANT_WORLD_DEFORMATION, 0.0f);
        hallucinationValues.put(HALLUCATION_BLOOM, 0.0f);
        hallucinationValues.put(HALLUCATION_COLOR_BLOOM, 0.0f);
        hallucinationValues.put(HALLUCATION_COLOR_CONTRAST, 0.0f);
    }

    public void update(LivingEntity entity, DrugProperties drugProperties)
    {
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

        for (Integer hKey : hallucinationValues.keySet())
        {
            float val = hallucinationValues.get(hKey);

            if (activeHallucinations.contains(hKey))
            {
                float desiredValue = randomColor(random, drugProperties.ticksExisted, getHallucinationMultiplier(hKey), 0.5f, 0.00121f, 0.0019318f);

                val = MathUtils.nearValue(val, desiredValue, 0.002f, 0.002f);
                hallucinationValues.put(hKey, val);
            }
            else
            {
                val = MathUtils.nearValue(val, 0.0f, 0.002f, 0.002f);
                hallucinationValues.put(hKey, val);
            }
        }

        currentMindColor[0] = MathUtils.nearValue(currentMindColor[0], randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f);
        currentMindColor[1] = MathUtils.nearValue(currentMindColor[1], randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0011239f, 0.0019321f), 0.002f, 0.002f);
        currentMindColor[2] = MathUtils.nearValue(currentMindColor[2], randomColor(random, drugProperties.ticksExisted, 0.5f, 0.5f, 0.0011541f, 0.0018682f), 0.002f, 0.002f);
    }

    public void updateEntities(LivingEntity entity, DrugProperties drugProperties, Random random)
    {
        float hallucinationChance = getHallucinationStrength(drugProperties, 1.0f) * 0.05f;
        if (hallucinationChance > 0.0f)
        {
            if (random.nextInt((int) (1F / hallucinationChance)) == 0)
            {
                if (entity instanceof PlayerEntity)
                {
                    addRandomEntityHallucination((PlayerEntity) entity, drugProperties, random);
                }
            }
        }

        for (Iterator<DrugHallucination> iterator = entities.iterator(); iterator.hasNext(); )
        {
            DrugHallucination hallucination = iterator.next();
            hallucination.update();

            if (hallucination.isDead())
                iterator.remove();
        }
    }

    public void addRandomEntityHallucination(PlayerEntity player, DrugProperties drugProperties, Random random)
    {
        if (!player.world.isClient)
        {
            return;
        }

        if (getNumberOfHallucinations(DrugHallucinationRastaHead.class) == 0 && (random.nextFloat() < 0.1f && drugProperties.getDrugValue("Cannabis") > 0.4f))
        {
            entities.add(new DrugHallucinationRastaHead(player));
        }
        else
        {
            entities.add(new DrugHallucinationEntity(player));
        }
    }

    public int getNumberOfHallucinations(Class<? extends DrugHallucination> aClass)
    {
        int count = 0;
        for (DrugHallucination hallucination : entities)
        {
            if (aClass.isAssignableFrom(hallucination.getClass()))
                count++;
        }

        return count;
    }

    public void removeRandomHallucination(Random random)
    {
        activeHallucinations.remove(activeHallucinations.get(random.nextInt(activeHallucinations.size())));
    }

    public boolean addRandomHallucination(Random random)
    {
        float maxValue = 0.0f;
        int currentHallucination = -1;

        for (int hKey : hallucinationValues.keySet())
        {
            if (!activeHallucinations.contains(hKey))
            {
                float value = random.nextFloat() * getHallucinationMultiplier(hKey);

                if (value > maxValue)
                {
                    currentHallucination = hKey;
                    maxValue = value;
                }
            }
        }

        if (currentHallucination >= 0)
            activeHallucinations.add(currentHallucination);
        return currentHallucination >= 0;
    }

    public float getHallucinationMultiplier(int hallucination)
    {
        float value = 1;
        for (HallucinationType type : hallucinationTypes) {
            if (type.hallucinations.contains(hallucination)) {
                value *= MathHelper.lerp(type.currentValue, 0, 0.5F);
            }
        }
        return value;
    }

    public void receiveChatMessage(LivingEntity entity, String message)
    {
        for (DrugHallucination h : entities)
        {
            h.receiveChatMessage(message, entity);
        }
    }

    public float getHallucinationStrength(DrugProperties drugProperties, float partialTicks)
    {
        return 0.4f * getHallucinationMultiplier(HALLUCATION_ENTITIES) * hallucinationValues.get(HALLUCATION_ENTITIES);
    }

    public float getDesaturation(DrugProperties drugProperties, float partialTicks)
    {
        float value = getHallucinationMultiplier(HALLUCATION_DESATURATION) * hallucinationValues.get(HALLUCATION_DESATURATION);
        for (Drug drug : drugProperties.getAllDrugs())
            value += (1.0f - value) * drug.desaturationHallucinationStrength();
        return value;
    }

    public float getColorIntensification(DrugProperties drugProperties, float partialTicks)
    {
        float value = getHallucinationMultiplier(HALLUCATION_SUPER_SATURATION) * hallucinationValues.get(HALLUCATION_SUPER_SATURATION);
        for (Drug drug : drugProperties.getAllDrugs())
            value += (1.0f - value) * drug.superSaturationHallucinationStrength();
        return value;
    }

    public float getSlowColorRotation(DrugProperties drugProperties, float partialTicks)
    {
        return getHallucinationMultiplier(HALLUCATION_SLOW_COLOR_ROTATION) * hallucinationValues.get(HALLUCATION_SLOW_COLOR_ROTATION);
    }

    public float getQuickColorRotation(DrugProperties drugProperties, float partialTicks)
    {
        return getHallucinationMultiplier(HALLUCATION_QUICK_COLOR_ROTATION) * hallucinationValues.get(HALLUCATION_QUICK_COLOR_ROTATION);
    }

    public float getBigWaveStrength(DrugProperties drugProperties, float partialTicks)
    {
        return 0.6f * getHallucinationMultiplier(HALLUCATION_BIG_WAVES) * hallucinationValues.get(HALLUCATION_BIG_WAVES);
    }

    public float getSmallWaveStrength(DrugProperties drugProperties, float partialTicks)
    {
        return 0.5f * getHallucinationMultiplier(HALLUCATION_SMALL_WAVES) * hallucinationValues.get(HALLUCATION_SMALL_WAVES);
    }

    public float getWiggleWaveStrength(DrugProperties drugProperties, float partialTicks)
    {
        return 0.7f * getHallucinationMultiplier(HALLUCATION_WIGGLE_WAVES) * hallucinationValues.get(HALLUCATION_WIGGLE_WAVES);
    }

    public float getSurfaceFractalStrength(DrugProperties drugProperties, float partialTicks)
    {
        return getHallucinationMultiplier(HALLUCATION_SURFACE_FRACTALS) * hallucinationValues.get(HALLUCATION_SURFACE_FRACTALS);
    }

    public float getDistantWorldDeformationStrength(DrugProperties drugProperties, float partialTicks)
    {
        return getHallucinationMultiplier(HALLUCATION_DISTANT_WORLD_DEFORMATION) * hallucinationValues.get(HALLUCATION_DISTANT_WORLD_DEFORMATION);
    }

    public void applyPulseColor(DrugProperties drugProperties, float[] pulseColor, float partialTicks)
    {
        float val = getHallucinationMultiplier(HALLUCATION_PULSES) * hallucinationValues.get(HALLUCATION_PULSES);
        pulseColor[0] = currentMindColor[0];
        pulseColor[1] = currentMindColor[1];
        pulseColor[2] = currentMindColor[2];
        pulseColor[3] = val;
    }

    public void applyColorBloom(DrugProperties drugProperties, float[] bloomColor, float partialTicks)
    {
        for (Drug drug : drugProperties.getAllDrugs())
            drug.applyColorBloom(bloomColor);

        float val = 1.5f * getHallucinationMultiplier(HALLUCATION_COLOR_BLOOM) * hallucinationValues.get(HALLUCATION_COLOR_BLOOM);
        mixColorsDynamic(currentMindColor, bloomColor, MathHelper.clamp(val, 0, 1));
    }

    public void applyContrastColorization(DrugProperties drugProperties, float[] contrastColor, float partialTicks)
    {
        for (Drug drug : drugProperties.getAllDrugs())
            drug.applyContrastColorization(contrastColor);

        float val = getHallucinationMultiplier(HALLUCATION_COLOR_CONTRAST) * hallucinationValues.get(HALLUCATION_COLOR_CONTRAST);
        mixColorsDynamic(currentMindColor, contrastColor, MathHelper.clamp(val, 0, 1));
    }

    public float getBloom(DrugProperties drugProperties, float partialTicks)
    {
        float value = getHallucinationMultiplier(HALLUCATION_BLOOM) * hallucinationValues.get(HALLUCATION_BLOOM);
        for (Drug drug : drugProperties.getAllDrugs())
            value += drug.bloomHallucinationStrength();
        return value;
    }

    public float getMotionBlur(DrugProperties drugProperties, float partialTicks)
    {
        float value = 0.0f;
        for (Drug drug : drugProperties.getAllDrugs())
            value += (1.0f - value) * drug.motionBlur();
        return value;
    }

    public static abstract class HallucinationType
    {
        public final List<Integer> hallucinations = new ArrayList<>();
        public float currentValue;

        public abstract float getDesiredValue(DrugProperties drugProperties);
    }
}
