package ivorius.psychedelicraft.advancement;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange.DoubleRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

public class DrugEffectsChangedCriterion extends AbstractCriterion<DrugEffectsChangedCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(
                playerPredicate,
                JsonHelper.getArray(json, "drugs").asList().stream().map(Conditions.DrugPredicate::of).toList()
        );
    }

    public void trigger(DrugProperties properties) {
        if (properties.asEntity() instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, properties));
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final List<DrugPredicate> drugs;

        public Conditions(Optional<LootContextPredicate> playerPredicate, List<DrugPredicate> drugs) {
            super(playerPredicate);
            this.drugs = drugs;
        }

        public boolean test(ServerPlayerEntity player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            JsonArray drugsJson = new JsonArray();
            drugs.forEach(drug -> drugsJson.add(drug.toJson()));
            json.add("drugs", drugsJson);
            return json;
        }

        public record DrugPredicate (DrugType type, DoubleRange range) implements Predicate<DrugProperties> {
            static DrugPredicate of(JsonElement json) {
                if (json.isJsonObject()) {
                    return new DrugPredicate(
                            DrugType.REGISTRY.get(new Identifier(JsonHelper.getString(json.getAsJsonObject(), "id"))),
                            DoubleRange.fromJson(json.getAsJsonObject().get("value"))
                    );
                }

                return new DrugPredicate(
                        DrugType.REGISTRY.get(new Identifier(json.getAsString())),
                        DoubleRange.atLeast(MathHelper.EPSILON)
                );
            }

            @Override
            public boolean test(DrugProperties properties) {
                return range.test(properties.getDrugValue(type));
            }

            public JsonObject toJson() {
                JsonObject json = new JsonObject();
                json.addProperty("id", type.id().toString());
                json.add("value", range.toJson());
                return json;
            }
        }
    }
}
