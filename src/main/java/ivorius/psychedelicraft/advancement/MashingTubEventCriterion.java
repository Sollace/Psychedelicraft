package ivorius.psychedelicraft.advancement;

import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

public class MashingTubEventCriterion extends AbstractCriterion<MashingTubEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(PlayerEntity player, ItemStack stack) {
        if (player instanceof ServerPlayerEntity p) {
            trigger(p, c -> c.test(p, stack));
        }
    }

    public interface Trigger {
        void trigger(@Nullable PlayerEntity player);
    }

    public record Conditions (Optional<LootContextPredicate> player, AlcoholicFluid fluid, IntRange fermentation, IntRange maturation, IntRange distillation) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
                SimpleFluid.CODEC.xmap(AlcoholicFluid.class::cast, Function.identity()).fieldOf("fluid").forGetter(Conditions::fluid),
                IntRange.CODEC.optionalFieldOf("fermentation", IntRange.ANY).forGetter(Conditions::fermentation),
                IntRange.CODEC.optionalFieldOf("maturation", IntRange.ANY).forGetter(Conditions::maturation),
                IntRange.CODEC.optionalFieldOf("distillation", IntRange.ANY).forGetter(Conditions::distillation)
        ).apply(instance, Conditions::new));

        public boolean test(ServerPlayerEntity player, ItemStack stack) {
            return FluidContainer.of(stack).getFluid(stack) == fluid
                    && fermentation.test(AlcoholicFluid.FERMENTATION.get(stack))
                    && maturation.test(AlcoholicFluid.MATURATION.get(stack))
                    && distillation.test(AlcoholicFluid.DISTILLATION.get(stack));
        }
    }
}
