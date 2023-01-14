package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Predicate;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.util.math.random.Random;

public class EntityHallucinationList implements Iterable<Hallucination> {
    private final List<Hallucination> entities = new ArrayList<>();
    private final List<Hallucination> pending = new ArrayList<>();

    private final HallucinationManager manager;

    EntityHallucinationList(HallucinationManager manager) {
        this.manager = manager;
    }

    public void update() {
        DrugProperties properties = manager.getProperties();
        Random random = properties.asEntity().getRandom();
        float hallucinationChance = manager.getHallucinationStrength(1) * 0.05f;

        if (hallucinationChance > 0 && random.nextInt((int) (1F / hallucinationChance)) == 0) {
            spawnHallucination(properties, random);
        }

        swap();
        pending.clear();
        entities.removeIf(hallucination -> {
            hallucination.update();
            return hallucination.isDead();
        });
        swap();
    }

    private void swap() {
        entities.addAll(pending);
        pending.clear();
    }

    private void spawnHallucination(DrugProperties properties, Random random) {
        if (!properties.asEntity().world.isClient) {
            return;
        }

        if (getNumberOfHallucinations(a -> a instanceof RastaHeadHallucination) == 0 && (random.nextFloat() < 0.1f && properties.getDrugValue(DrugType.CANNABIS) > 0.4f)) {
            pending.add(new RastaHeadHallucination(properties.asEntity()));
        } else {
            pending.add(new EntityHallucination(properties.asEntity()));
        }
    }

    private int getNumberOfHallucinations(Predicate<Hallucination> test) {
        int count = 0;
        for (Hallucination hallucination : this) {
            if (test.test(hallucination)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Iterator<Hallucination> iterator() {
        return entities.iterator();
    }
}
