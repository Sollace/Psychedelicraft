package ivorius.psychedelicraft.advancement;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange.DoubleRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class DrugEffectsChangedCriterion extends AbstractCriterion<DrugEffectsChangedCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(DrugProperties properties) {
        if (properties.asEntity() instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, properties));
        }
    }

    public record Conditions(Optional<LootContextPredicate> player, List<DrugPredicate> drugs) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                DrugPredicate.CODEC.listOf().fieldOf("drugs").forGetter(Conditions::drugs)
        ).apply(instance, Conditions::new));

        public boolean test(ServerPlayerEntity player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        public record DrugPredicate (DrugType type, DoubleRange range) implements Predicate<DrugProperties> {
            public static final Codec<DrugPredicate> CODEC = Codec.either(
                    DrugType.REGISTRY.getCodec().xmap(id -> new DrugPredicate(id, DoubleRange.atLeast(MathHelper.EPSILON)), DrugPredicate::type),
                    RecordCodecBuilder.<DrugPredicate>create(instance -> instance.group(
                            DrugType.REGISTRY.getCodec().fieldOf("id").forGetter(DrugPredicate::type),
                            DoubleRange.CODEC.fieldOf("value").forGetter(DrugPredicate::range)
                    ).apply(instance, DrugPredicate::new))
            ).xmap(pair -> {
                return pair.left().or(pair::right).get();
            }, predicate -> Either.right(predicate));

            @Override
            public boolean test(DrugProperties properties) {
                return range.test(properties.getDrugValue(type));
            }
        }
    }
}
