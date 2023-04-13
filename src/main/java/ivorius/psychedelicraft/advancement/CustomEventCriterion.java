package ivorius.psychedelicraft.advancement;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    private static final Identifier ID = Psychedelicraft.id("custom");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, Extended playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
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

        public Conditions(Extended playerPredicate, String event) {
            super(ID, playerPredicate);
            this.event = event;
        }

        public boolean test(ServerPlayerEntity player, String event) {
            return this.event.contentEquals(event);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("event", event);
            return json;
        }
    }
}
