package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(playerPredicate, JsonHelper.getString(json, "event"));
    }

    public CustomEventCriterion.Trigger createTrigger(String event) {
        return player -> {
            if (player instanceof ServerPlayerEntity p) {
                trigger(p, c -> c.test(p, event));
            }
        };
    }

    public interface Trigger {
        void trigger(@Nullable PlayerEntity player);
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final String event;

        public Conditions(Optional<LootContextPredicate> playerPredicate, String event) {
            super(playerPredicate);
            this.event = event;
        }

        public boolean test(ServerPlayerEntity player, String event) {
            return this.event.contentEquals(event);
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.addProperty("event", event);
            return json;
        }
    }
}
