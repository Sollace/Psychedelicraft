package ivorius.psychedelicraft.advancement;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class MashingTubEventCriterion extends AbstractCriterion<MashingTubEventCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> predicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(predicate,
                (AlcoholicFluid)SimpleFluid.REGISTRY.get(Identifier.tryParse(JsonHelper.getString(json, "fluid", "r"))),
                IntRange.fromJson(json.get("fermentation")),
                IntRange.fromJson(json.get("maturation")),
                IntRange.fromJson(json.get("distillation"))
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

    public record Conditions(Optional<LootContextPredicate> getPlayerPredicate, AlcoholicFluid fluid, IntRange fermentation, IntRange maturation, IntRange distillation) implements AbstractCriterion.Conditions {
        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fluid", fluid.getId().toString());
            json.add("fermentation", fermentation.toJson());
            json.add("maturation", maturation.toJson());
            json.add("distillation", distillation.toJson());
            return json;
        }

        public static AdvancementCriterion<Conditions> create(AlcoholicFluid fluid) {
            return PSCriteria.SIMPLY_MASHING.create(new Conditions(Optional.empty(), fluid, IntRange.ANY, IntRange.ANY, IntRange.ANY));
        }

        public static AdvancementCriterion<Conditions> create(AlcoholicFluid fluid, IntRange fermentation, IntRange maturation, IntRange distillation) {
            return PSCriteria.SIMPLY_MASHING.create(new Conditions(Optional.empty(), fluid, fermentation, maturation, distillation));
        }

        public boolean test(ServerPlayerEntity player, ItemStack stack) {
            return ItemFluids.of(stack).fluid() == fluid
                    && fermentation.test(AlcoholicFluid.FERMENTATION.get(stack))
                    && maturation.test(AlcoholicFluid.MATURATION.get(stack))
                    && distillation.test(AlcoholicFluid.DISTILLATION.get(stack));
        }
    }
}
