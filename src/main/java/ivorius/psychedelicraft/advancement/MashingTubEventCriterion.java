package ivorius.psychedelicraft.advancement;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.*;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MashingTubEventCriterion extends AbstractCriterion<MashingTubEventCriterion.Conditions> {

    private static final Identifier ID = Psychedelicraft.id("mashed_item");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, Extended playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
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

        public Conditions(Extended playerPredicate, AlcoholicFluid fluid, IntRange fermentation, IntRange maturation, IntRange distillation) {
            super(ID, playerPredicate);
            this.fluid = fluid;
            this.fermentation = fermentation;
            this.maturation = maturation;
            this.distillation = distillation;
        }

        public boolean test(ServerPlayerEntity player, ItemStack stack) {
            return FluidContainerItem.of(stack).getFluid(stack) == fluid
                    && fermentation.test(fluid.getFermentation(stack))
                    && maturation.test(fluid.getMaturation(stack))
                    && distillation.test(fluid.getDistillation(stack));
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("fluid", fluid.getId().toString());
            json.add("fermentation", fermentation.toJson());
            json.add("maturation", maturation.toJson());
            json.add("distillation", distillation.toJson());
            return json;
        }
    }
}
