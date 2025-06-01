package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var o = LootContextPredicate.fromJson("entity", predicateDeserializer, obj.get("entity"), LootContextTypes.ADVANCEMENT_ENTITY).flatMap(i -> i);
        return new Conditions(predicate, o, JsonHelper.getString(obj, "event"));
    }

    public CustomEventCriterion.Trigger createTrigger(String event) {
        return (player, entity) -> {
            if (player instanceof ServerPlayerEntity p) {
                trigger(p, c -> c.test(entity == null ? null : EntityPredicate.createAdvancementEntityLootContext(p, entity), event));
            }
        };
    }

    public interface Trigger {
        default void trigger(@Nullable PlayerEntity player) {
            trigger(player, null);
        }

        void trigger(@Nullable PlayerEntity player, @Nullable Entity target);
    }

    public record Conditions(Optional<LootContextPredicate> getPlayerPredicate, Optional<LootContextPredicate> entity, String event) implements AbstractCriterion.Conditions {
        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("event", event);
            entity.ifPresent(e -> json.add("entity", e.toJson()));
            return json;
        }

        public static AdvancementCriterion<Conditions> create(String event) {
            return PSCriteria.CUSTOM.create(new Conditions(Optional.empty(), Optional.empty(), event));
        }

        public static AdvancementCriterion<Conditions> create(String event, LootContextPredicate entity) {
            return PSCriteria.CUSTOM.create(new Conditions(Optional.empty(), Optional.of(entity), event));
        }

        public boolean test(LootContext entity, String event) {
            return this.event.contentEquals(event) && (this.entity().isEmpty() || (entity != null && this.entity().get().test(entity)));
        }
    }
}
