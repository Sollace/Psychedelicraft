package ivorius.psychedelicraft.advancement;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    static final Identifier ID = Psychedelicraft.id("custom");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var o = LootContextPredicate.fromJson("entity", predicateDeserializer, obj.get("entity"), LootContextTypes.ADVANCEMENT_ENTITY);
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

    public static class Conditions extends AbstractCriterionConditions {
        private final LootContextPredicate entity;
        private final String event;

        public Conditions(LootContextPredicate player, LootContextPredicate entity, String event) {
            super(ID, player);
            this.entity = entity;
            this.event = event;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("event", event);
            if (entity != null) {
                json.add("entity", entity.toJson(serializer));
            }
            return json;
        }

        public static Conditions create(String event) {
            return new Conditions(LootContextPredicate.EMPTY, null, event);
        }

        public static Conditions create(String event, LootContextPredicate entity) {
            return new Conditions(LootContextPredicate.EMPTY, entity, event);
        }

        public boolean test(LootContext entity, String event) {
            return this.event.contentEquals(event) && (this.entity != null || (entity != null && this.entity.test(entity)));
        }
    }
}
