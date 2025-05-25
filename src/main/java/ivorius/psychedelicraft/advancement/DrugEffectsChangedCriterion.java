package ivorius.psychedelicraft.advancement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange.DoubleRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class DrugEffectsChangedCriterion extends AbstractCriterion<DrugEffectsChangedCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate, Conditions.DRUGS_CODEC.decode(JsonOps.INSTANCE, obj.get("drugs")).result().map(Pair::getFirst).orElseGet(List::of));
    }

    public void trigger(DrugProperties properties) {
        if (properties.asEntity() instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, properties));
        }
    }

    public record Conditions(Optional<LootContextPredicate> getPlayerPredicate, List<DrugPredicate> drugs) implements AbstractCriterion.Conditions {
        static final Codec<List<DrugPredicate>> DRUGS_CODEC = DrugPredicate.CODEC.listOf();
        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            DRUGS_CODEC.encodeStart(JsonOps.INSTANCE, drugs).result().ifPresent(a -> {
                json.add("drugs", a);
            });
            return json;
        }

        public static AdvancementCriterion<Conditions> create(Collection<DrugType<?>> types) {
            return PSCriteria.DRUG_EFFECTS_CHANGED.create(new Conditions(Optional.empty(), types.stream().map(type -> new DrugPredicate(type, DoubleRange.atLeast(MathHelper.EPSILON))).toList()));
        }

        public boolean test(ServerPlayerEntity player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        public record DrugPredicate (DrugType<?> type, DoubleRange range) implements Predicate<DrugProperties> {
            public static final Codec<DrugPredicate> CODEC = Codec.either(
                    DrugType.REGISTRY.getCodec().xmap(id -> new DrugPredicate(id, DoubleRange.atLeast(MathHelper.EPSILON)), DrugPredicate::type),
                    RecordCodecBuilder.<DrugPredicate>create(instance -> instance.group(
                            DrugType.REGISTRY.getCodec().fieldOf("id").forGetter(DrugPredicate::type),
                            DoubleRange.CODEC.fieldOf("value").forGetter(DrugPredicate::range)
                    ).apply(instance, DrugPredicate::new))
            ).xmap(either -> either.left().or(either::right).orElseThrow(), Either::right);

            @Override
            public boolean test(DrugProperties properties) {
                return range.test(properties.getDrugValue(type));
            }
        }
    }
}
