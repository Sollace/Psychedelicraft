package ivorius.psychedelicraft.advancement;

import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange.FloatRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

public class DrugEffectsChangedCriterion extends AbstractCriterion<DrugEffectsChangedCriterion.Conditions> {
    private static final Identifier ID = Psychedelicraft.id("drug_effects_changed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
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

        public Conditions(LootContextPredicate playerPredicate, List<DrugPredicate> drugs) {
            super(ID, playerPredicate);
            this.drugs = drugs;
        }

        public boolean test(ServerPlayerEntity player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            JsonArray drugsJson = new JsonArray();
            drugs.forEach(drug -> drugsJson.add(drug.toJson()));
            json.add("drugs", drugsJson);
            return json;
        }

        public record DrugPredicate (DrugType type, FloatRange range) implements Predicate<DrugProperties> {
            static DrugPredicate of(JsonElement json) {
                if (json.isJsonObject()) {
                    return new DrugPredicate(
                            DrugType.REGISTRY.get(new Identifier(JsonHelper.getString(json.getAsJsonObject(), "id"))),
                            FloatRange.fromJson(json.getAsJsonObject().get("value"))
                    );
                }

                return new DrugPredicate(
                        DrugType.REGISTRY.get(new Identifier(json.getAsString())),
                        FloatRange.atLeast(MathHelper.EPSILON)
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
