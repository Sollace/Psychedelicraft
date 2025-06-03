package ivorius.psychedelicraft.world.gen.loot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetFluidsLootFunction extends ConditionalLootFunction {
    private final List<SimpleFluid> fluid;
    private final Map<String, LootNumberProvider> attributes;

    protected SetFluidsLootFunction(LootCondition[] conditions, Collection<SimpleFluid> fluid,  Map<String, LootNumberProvider> attributes) {
        super(conditions);
        this.fluid = fluid.stream().distinct().toList();
        this.attributes = attributes;
    }

    @Override
    public LootFunctionType getType() {
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

    public static class Serializer extends ConditionalLootFunction.Serializer<SetFluidsLootFunction> {
        @Override
        public void toJson(JsonObject json, SetFluidsLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            JsonArray fluids = new JsonArray();
            function.fluid.forEach(f -> {
                fluids.add(f.getId().toString());
            });
            json.add("fluid", fluids);
            JsonObject attributes = new JsonObject();
            function.attributes.forEach((attribute, value) -> {
                attributes.add(attribute, context.serialize(value));
            });
            json.add("attributes", attributes);
        }

        @Override
        public SetFluidsLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            List<SimpleFluid> fluid = JsonHelper.getArray(json, "fluid").asList().stream().map(i -> SimpleFluid.REGISTRY.get(Identifier.tryParse(i.getAsString()))).distinct().toList();
            Map<String, LootNumberProvider> attributes = JsonHelper.getObject(json, "attributes", new JsonObject()).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, i -> {
                return context.deserialize(i.getValue(), LootNumberProvider.class);
            }));
            return new SetFluidsLootFunction(conditions, fluid, attributes);
        }
    }
}
