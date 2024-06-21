package ivorius.psychedelicraft.entity.drug.hallucination;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class Visualisations {

    private final HallucinationTypes hallucinationTypes = new HallucinationTypes();

    private final IntList activeHallucinations = new IntArrayList();
    private final Int2FloatMap hallucinationStrengths = new Int2FloatOpenHashMap();

    public float getMultiplier(int hallucination) {
        return hallucinationTypes.getMultiplier(hallucination) * hallucinationStrengths.get(hallucination);
    }

    void update(DrugProperties properties) {
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
            hallucinationStrengths.compute(hKey, (key, val) -> MathUtils.nearValue(val == null ? 0 : val, activeHallucinations.contains(key.intValue())
                    ? MathUtils.randomColor(random, properties.getAge(), hallucinationTypes.getMultiplier(key), 0.5f, 0.00121f, 0.0019318f)
                    : 0, 0.002f, 0.002f
            ));
        }
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

}
