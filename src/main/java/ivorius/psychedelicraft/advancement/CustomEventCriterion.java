package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
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

    public record Conditions (Optional<LootContextPredicate> player, Optional<LootContextPredicate> entity, String event) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity),
                Codec.STRING.fieldOf("event").forGetter(Conditions::event)
        ).apply(instance, Conditions::new));

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
