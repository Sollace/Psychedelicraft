package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.Pool;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.NumberRange.DoubleRange;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public record EntityHallucinationType (Identifier id, Function<PlayerEntity, Hallucination> factory, float chance, DoubleRange strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
    public static final Map<Identifier, EntityHallucinationType> REGISTRY = new HashMap<>();

    static {
        register(HallucinationTypeKeys.RASTA_HEAD, RastaHeadHallucination::new, 0.1F, DoubleRange.ANY, list -> {
            return list.getNumberOfHallucinations(a -> a instanceof RastaHeadHallucination) == 0 && list.getProperties().getDrugValue(DrugType.CANNABIS) > 0.4F;
        });
        register(HallucinationTypeKeys.MULTIPLE_ENTITY, MultipleEntityHallucination::new, 0.5F, DoubleRange.ANY);
        register(HallucinationTypeKeys.SINGLE_ENTITY, EntityHallucination::new, 1, DoubleRange.ANY);
        register(HallucinationTypeKeys.HOSTILE_VILLAGERS, (player -> new EntityIdentitySwapHallucination(player, EntityType.VILLAGER, Pool.create(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER))), 1, DoubleRange.atLeast(0.8F));
        register(HallucinationTypeKeys.FRIENDLY_ZOMBIES, (player -> new EntityIdentitySwapHallucination(player, EntityType.ZOMBIE, Pool.create(EntityType.VILLAGER))), 1, DoubleRange.atLeast(0.8F));
    }

    public static Stream<EntityHallucinationType> getCandidates(EntityHallucinationList list) {
        Random weight = list.getProperties().asEntity().getRandom();
        return REGISTRY.values().stream()
                .filter(i -> i.strengthRange().test(list.getManager().getEntityHallucinationStrength())
                            && weight.nextFloat() <= i.chance()
                            && (i.condition() == null || i.condition().test(list)
                ));
    }

    static void register(Identifier id, Function<PlayerEntity, Hallucination> factory, float chance, DoubleRange strengthRange) {
        register(id, factory, chance, DoubleRange.ANY, null);
    }

    static void register(Identifier id, Function<PlayerEntity, Hallucination> factory, float chance, DoubleRange strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
        REGISTRY.put(id, new EntityHallucinationType(id, factory, chance, strengthRange, condition));
    }
}
