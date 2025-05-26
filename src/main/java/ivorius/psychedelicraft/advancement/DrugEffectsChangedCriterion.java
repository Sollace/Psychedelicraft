package ivorius.psychedelicraft.advancement;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.compat.RangeCompat;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange.FloatRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DrugEffectsChangedCriterion extends AbstractCriterion<DrugEffectsChangedCriterion.Conditions> {
    static final Identifier ID = Psychedelicraft.id("drug_effects_changed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate, Conditions.DRUGS_CODEC.decode(JsonOps.INSTANCE, obj.get("drugs")).result().map(Pair::getFirst).orElseGet(List::of));
    }

    public void trigger(DrugProperties properties) {
        if (properties.asEntity() instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, properties));
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        static final Codec<List<DrugPredicate>> DRUGS_CODEC = DrugPredicate.CODEC.listOf();

        private final List<DrugPredicate> drugs;

        public Conditions(LootContextPredicate entity, List<DrugPredicate> drugs) {
            super(ID, entity);
            this.drugs = drugs;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            DRUGS_CODEC.encodeStart(JsonOps.INSTANCE, drugs).result().ifPresent(a -> {
                json.add("drugs", a);
            });
            return json;
        }

        public static Conditions create(Collection<DrugType<?>> types) {
            return new Conditions(LootContextPredicate.EMPTY, types.stream().map(type -> new DrugPredicate(type, FloatRange.atLeast(MathHelper.EPSILON))).toList());
        }

        public boolean test(ServerPlayerEntity player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        public record DrugPredicate (DrugType<?> type, FloatRange range) implements Predicate<DrugProperties> {
            public static final Codec<DrugPredicate> CODEC = Codec.either(
                    DrugType.REGISTRY.getCodec().xmap(id -> new DrugPredicate(id, FloatRange.atLeast(MathHelper.EPSILON)), DrugPredicate::type),
                    RecordCodecBuilder.<DrugPredicate>create(instance -> instance.group(
                            DrugType.REGISTRY.getCodec().fieldOf("id").forGetter(DrugPredicate::type),
                            RangeCompat.FLOAT_CODEC.fieldOf("value").forGetter(DrugPredicate::range)
                    ).apply(instance, DrugPredicate::new))
            ).xmap(either -> either.left().or(either::right).orElseThrow(), Either::right);

            @Override
            public boolean test(DrugProperties properties) {
                return range.test(properties.getDrugValue(type));
            }
        }
    }
}
