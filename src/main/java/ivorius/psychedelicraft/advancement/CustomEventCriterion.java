package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
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

    public record Conditions (Optional<LootContextPredicate> player, String event) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
                Codec.STRING.fieldOf("event").forGetter(Conditions::event)
        ).apply(instance, Conditions::new));

        public boolean test(ServerPlayerEntity player, String event) {
            return this.event.contentEquals(event);
        }
    }
}
