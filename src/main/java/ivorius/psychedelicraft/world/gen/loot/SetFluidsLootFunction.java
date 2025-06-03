package ivorius.psychedelicraft.world.gen.loot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public class SetFluidsLootFunction extends ConditionalLootFunction {
    public static final MapCodec<SetFluidsLootFunction> CODEC = RecordCodecBuilder.mapCodec(i -> addConditionsField(i).and(i.group(
        Codec.list(SimpleFluid.CODEC).fieldOf("fluid").forGetter(o -> o.fluid),
        Codec.unboundedMap(Codec.STRING, LootNumberProviderTypes.CODEC).fieldOf("attributes").forGetter(o -> o.attributes)
    )).apply(i, SetFluidsLootFunction::new));

    private final List<SimpleFluid> fluid;
    private final Map<String, LootNumberProvider> attributes;

    protected SetFluidsLootFunction(List<LootCondition> conditions, Collection<SimpleFluid> fluid,  Map<String, LootNumberProvider> attributes) {
        super(conditions);
        this.fluid = fluid.stream().distinct().toList();
        this.attributes = attributes;
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return PSLootFunctionTypes.SET_FLUIDS;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        ItemFluids fluids = fluid.get(context.getRandom().nextInt(fluid.size())).getDefaultStack(FluidCapacity.get(stack));
        Map<String, Integer> atrs = new HashMap<>(fluids.attributes());
        attributes.forEach((attribute, value) -> atrs.put(attribute, value.nextInt(context)));
        return ItemFluids.set(stack, fluids.withAttributes(atrs));
    }

    public static Builder builder(SimpleFluid fluid) {
        return builder(Set.of(fluid));
    }

    public static Builder builder(Set<SimpleFluid> fluid) {
        return new Builder(fluid);
    }

    public static class Builder extends ConditionalLootFunction.Builder<Builder> {
        private final Set<SimpleFluid> fluid;
        private final Map<String, LootNumberProvider> attributes = new HashMap<>();

        public Builder(Set<SimpleFluid> fluid) {
            this.fluid = fluid;
        }

        public Builder attribute(SimpleFluid.Attribute<Integer> attribute, LootNumberProvider value) {
            attributes.put(attribute.name(), value);
            return this;
        }

        public Builder attribute(SimpleFluid.Attribute<Boolean> attribute, boolean value) {
            attributes.put(attribute.name(), ConstantLootNumberProvider.create(value ? 1 : 0));
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetFluidsLootFunction(getConditions(), fluid, attributes);
        }
    }
}
