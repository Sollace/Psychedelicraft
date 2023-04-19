package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.Pool;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.NumberRange.FloatRange;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public record EntityHallucinationType (Identifier id, Function<PlayerEntity, Hallucination> factory, float chance, FloatRange strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
    public static final Map<Identifier, EntityHallucinationType> REGISTRY = new HashMap<>();

    public static final EntityHallucinationType RASTA_HEAD = register("rasta_head", RastaHeadHallucination::new, 0.1F, FloatRange.ANY, list -> {
        return list.getNumberOfHallucinations(a -> a instanceof RastaHeadHallucination) == 0 && list.getProperties().getDrugValue(DrugType.CANNABIS) > 0.4F;
    });
    public static final EntityHallucinationType MULTIPLE_ENTITY = register("multiple_entity", MultipleEntityHallucination::new, 0.5F, FloatRange.ANY);
    public static final EntityHallucinationType SINGLE_ENTITY = register("single_entity", EntityHallucination::new, 1, FloatRange.ANY);
    public static final EntityHallucinationType HOSTILE_VILLAGERS = register("hostile_villagers", (player -> new EntityIdentitySwapHallucination(player, EntityType.VILLAGER, Pool.create(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER))), 1, FloatRange.atLeast(0.8F));
    public static final EntityHallucinationType FRIENDLY_ZOMBIES = register("friendly_zombies", (player -> new EntityIdentitySwapHallucination(player, EntityType.ZOMBIE, Pool.create(EntityType.VILLAGER))), 1, FloatRange.atLeast(0.8F));

    public static Stream<EntityHallucinationType> getCandidates(EntityHallucinationList list) {
        Random weight = list.getProperties().asEntity().getRandom();
        return REGISTRY.values().stream()
                .filter(i -> i.strengthRange().test(list.getManager().getHallucinationStrength(1))
                            && weight.nextFloat() <= i.chance()
                            && (i.condition() == null || i.condition().test(list)
                ));
    }

    static EntityHallucinationType register(String name, Function<PlayerEntity, Hallucination> factory, float chance, FloatRange strengthRange) {
        return register(name, factory, chance, FloatRange.ANY, null);
    }

    static EntityHallucinationType register(String name, Function<PlayerEntity, Hallucination> factory, float chance, FloatRange strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
        var type = new EntityHallucinationType(Psychedelicraft.id(name), factory, chance, strengthRange, condition);
        REGISTRY.put(type.id(), type);
        return type;
    }
}
