package ivorius.psychedelicraft.recipe.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.RecipeUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record FluidIngredient (Optional<SimpleFluid> fluid, Optional<Integer> level, Map<String, Integer> attributes) implements CustomIngredient, Predicate<ItemStack> {
    public static final FluidIngredient EMPTY = new FluidIngredient(Optional.empty(), Optional.empty(), Map.of());
    public static final MapCodec<FluidIngredient> MAP_CODEC = RecordCodecBuilder.<FluidIngredient>mapCodec(instance -> instance.group(
            SimpleFluid.CODEC.optionalFieldOf("fluid").forGetter(FluidIngredient::fluid),
            Codec.INT.optionalFieldOf("level").forGetter(FluidIngredient::level),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("attributes", Map.of()).forGetter(FluidIngredient::attributes)
    ).apply(instance, FluidIngredient::new));
    public static final Codec<FluidIngredient> CODEC = Codec.either(
            SimpleFluid.CODEC.xmap(fluid -> new FluidIngredient(Optional.of(fluid), Optional.empty(), Map.of()), i -> i.fluid().orElse(PSFluids.EMPTY)),
            MAP_CODEC.codec()
        ).xmap(RecipeUtils::iDontCareWhich, Either::right);
    public static final PacketCodec<RegistryByteBuf, FluidIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(SimpleFluid.PACKET_CODEC), FluidIngredient::fluid,
            PacketCodecs.optional(PacketCodecs.INTEGER), FluidIngredient::level,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.INTEGER), FluidIngredient::attributes,
            FluidIngredient::new
    );

    public FluidIngredient {
        fluid = fluid.filter(f -> !f.isEmpty());
    }

    @Override
    public boolean test(ItemStack stack) {
        return test(ItemFluids.of(stack));
    }

    public boolean test(ItemFluids fluids) {
        boolean result = true;
        result &= fluid.isEmpty() || fluids.fluid() == fluid.get();
        result &= attributes.isEmpty() || attributes.equals(fluids.attributes());
        result &= level.isEmpty() || fluids.amount() >= level.get();
        return result;
    }

    public ItemFluids getAsItemFluid(int capacity) {
        return ItemFluids.create(fluid.orElse(PSFluids.EMPTY), level.orElse(capacity), attributes);
    }

    public boolean isEmpty() {
        return fluid.isEmpty() && level.isEmpty() && attributes.isEmpty();
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return allRecepticals()
                .map(receptical -> ItemFluids.set(receptical, getAsItemFluid(FluidCapacity.get(receptical))))
                .toList();
    }

    static Stream<ItemStack> allRecepticals() {
        return Stream.of(Items.BUCKET, Items.GLASS_BOTTLE, PSItems.BOTTLE, PSItems.WOODEN_MUG, PSItems.GLASS_CHALICE, PSItems.STONE_CUP, PSItems.SYRINGE)
                .map(Item::getDefaultStack);
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return PSIngredients.FLUID;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Optional<SimpleFluid> fluid = Optional.empty();
        private Optional<Integer> level = Optional.empty();
        private final Map<String, Integer> attributes = new HashMap<>();

        public Builder fluid(SimpleFluid fluid) {
            this.fluid = Optional.of(fluid);
            return this;
        }

        public Builder fluid(Fluid fluid) {
            return fluid(SimpleFluid.of(fluid));
        }

        public Builder level(int level) {
            this.level = Optional.of(level);
            return this;
        }

        public Builder attribute(String attribute, int value) {
            this.attributes.put(attribute, value);
            return this;
        }

        public FluidIngredient build() {
            return new FluidIngredient(fluid, level, Map.copyOf(attributes));
        }
    }
}

