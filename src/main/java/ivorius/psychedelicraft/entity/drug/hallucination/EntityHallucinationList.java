package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class EntityHallucinationList implements Iterable<Hallucination> {
    public static final Identifier TYPE_RASTA = Psychedelicraft.id("rasta_head");
    public static final Identifier TYPE_MOB = Psychedelicraft.id("mob");
    public static final Map<Identifier, Function<PlayerEntity, Hallucination>> TYPES = Map.of(
            TYPE_RASTA, RastaHeadHallucination::new,
            TYPE_MOB, EntityHallucination::new
    );

    private final List<Hallucination> entities = new ArrayList<>();
    private final List<Hallucination> pending = new ArrayList<>();

    private final HallucinationManager manager;

    EntityHallucinationList(HallucinationManager manager) {
        this.manager = manager;
    }

    public void update() {
        float hallucinationChance = manager.getHallucinationStrength(1) * 0.05f;

        if (hallucinationChance > 0 && manager.getProperties().asEntity().getRandom().nextInt((int) (1F / hallucinationChance)) == 0) {
            spawnHallucination();
        }

        swap();
        pending.clear();
        synchronized (entities) {
            entities.removeIf(hallucination -> {
                hallucination.update();
                return hallucination.isDead();
            });
        }
        swap();
    }

    private void swap() {
        synchronized (entities) {
            entities.addAll(pending);
            pending.clear();
        }
    }

    public void spawnHallucination() {
        DrugProperties properties = manager.getProperties();
        if (!properties.asEntity().world.isClient) {
            return;
        }

        Random random = properties.asEntity().getRandom();
        addHallucination((getNumberOfHallucinations(a -> a instanceof RastaHeadHallucination) == 0
                && random.nextFloat() < 0.1f
                && properties.getDrugValue(DrugType.CANNABIS) > 0.4f) ? TYPE_RASTA : TYPE_MOB
        );
    }

    public void addHallucination(Identifier type) {
        var factory = TYPES.get(type);
        if (factory != null) {
            pending.add(factory.apply(manager.getProperties().asEntity()));
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

    public List<ChatBot> getChatBots() {
        synchronized (entities) {
            return entities.stream().flatMap(i -> i.getChatBot().stream()).toList();
        }
    }
}
