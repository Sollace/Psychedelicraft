package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.function.Predicate;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class EntityHallucinationList implements Iterable<Hallucination> {
    private final List<Hallucination> entities = new ArrayList<>();
    private final List<Hallucination> pending = new ArrayList<>();

    private final HallucinationManager manager;

    private int prevForcedTicks;
    private int forcedTicks;

    EntityHallucinationList(HallucinationManager manager) {
        this.manager = manager;
    }

    public DrugProperties getProperties() {
        return manager.getProperties();
    }

    public HallucinationManager getManager() {
        return manager;
    }

    public void update() {
        float hallucinationChance = manager.getHallucinationStrength(1) * 0.05f;

        if (forcedTicks > 0) {
            prevForcedTicks = forcedTicks;
            forcedTicks--;
        }

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

    public <T extends Hallucination> List<T> getHallucinations(Class<T> type) {
        synchronized (entities) {
            return entities.stream().filter(a -> a.getClass() == type).map(type::cast).toList();
        }
    }

    private void swap() {
        synchronized (entities) {
            entities.addAll(pending);
            pending.clear();
        }
    }

    public float getForcedAlpha(float tickDelta) {
        float ticks = manager.getProperties().getAge() + tickDelta;
        float percent = MathHelper.lerp(tickDelta, prevForcedTicks, forcedTicks) / 400F;
        if (percent <= 0.01F) {
            return 0;
        }

        float baseAlpha = Math.abs(Math.min(MathHelper.sin(percent * MathHelper.HALF_PI * 2F) * 2, 1));
        if (percent < 0.1F) {
            baseAlpha += 0.5F + MathHelper.sin(ticks / 3F) / 2F;
            baseAlpha /= 2F;
        }

        return baseAlpha;
    }

    public void spawnHallucination() {
        if (getProperties().asEntity().world.isClient) {
            EntityHallucinationType.getCandidates(this).findFirst().ifPresent(type -> addHallucination(type, false));
        }
    }

    public void addHallucination(Identifier type, boolean force) {
        if (force) {
            forcedTicks = 400;
            prevForcedTicks = 400;
        }
        var factory = EntityHallucinationType.REGISTRY.get(type);
        if (factory != null) {
            addHallucination(factory, false);
        }
    }

    public void addHallucination(EntityHallucinationType type, boolean force) {
        if (force) {
            forcedTicks = 400;
            prevForcedTicks = 400;
        }

        pending.add(type.factory().apply(manager.getProperties().asEntity()));
    }

    public int getNumberOfHallucinations(Predicate<Hallucination> test) {
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
