package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate, JsonHelper.getString(obj, "event"));
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

    public record Conditions(Optional<LootContextPredicate> getPlayerPredicate, String event) implements AbstractCriterion.Conditions {
        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("event", event);
            return json;
        }

        public static AdvancementCriterion<Conditions> create(String event) {
            return PSCriteria.CUSTOM.create(new Conditions(Optional.empty(), event));
        }

        public boolean test(ServerPlayerEntity player, String event) {
            return this.event.contentEquals(event);
        }
    }
}
