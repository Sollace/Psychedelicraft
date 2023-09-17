package ivorius.psychedelicraft.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MashingTubEventCriterion extends AbstractCriterion<MashingTubEventCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(
                playerPredicate,
                (AlcoholicFluid)SimpleFluid.byId(Identifier.tryParse(json.get("fluid").getAsString())),
                json.has("fermentaion") ? IntRange.fromJson(json.get("fermentation")) : IntRange.ANY,
                json.has("maturation") ? IntRange.fromJson(json.get("maturation")) : IntRange.ANY,
                json.has("distillation") ? IntRange.fromJson(json.get("distillation")) : IntRange.ANY
        );
    }

    public void trigger(PlayerEntity player, ItemStack stack) {
        if (player instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, stack));
        }
    }

    public interface Trigger {
        void trigger(@Nullable PlayerEntity player);
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final AlcoholicFluid fluid;

        private final IntRange fermentation;
        private final IntRange maturation;
        private final IntRange distillation;

        public Conditions(Optional<LootContextPredicate> playerPredicate, AlcoholicFluid fluid, IntRange fermentation, IntRange maturation, IntRange distillation) {
            super(playerPredicate);
            this.fluid = fluid;
            this.fermentation = fermentation;
            this.maturation = maturation;
            this.distillation = distillation;
        }

        public boolean test(ServerPlayerEntity player, ItemStack stack) {
            return FluidContainer.of(stack).getFluid(stack) == fluid
                    && fermentation.test(AlcoholicFluid.FERMENTATION.get(stack))
                    && maturation.test(AlcoholicFluid.MATURATION.get(stack))
                    && distillation.test(AlcoholicFluid.DISTILLATION.get(stack));
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.addProperty("fluid", fluid.getId().toString());
            json.add("fermentation", fermentation.toJson());
            json.add("maturation", maturation.toJson());
            json.add("distillation", distillation.toJson());
            return json;
        }
    }
}
