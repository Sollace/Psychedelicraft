package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;

public record FluidModifyingResult(Map<String, Modification> attributes, ItemStack result) {
    public static final Codec<FluidModifyingResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Modification.CODEC).optionalFieldOf("attributes", Map.of()).forGetter(FluidModifyingResult::attributes),
            ItemStack.VALIDATED_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(FluidModifyingResult::result)
        ).apply(instance, FluidModifyingResult::new));
    public static final PacketCodec<RegistryByteBuf, FluidModifyingResult> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, Modification.PACKET_CODEC), FluidModifyingResult::attributes,
            ItemStack.OPTIONAL_PACKET_CODEC, FluidModifyingResult::result,
            FluidModifyingResult::new
    );

    public ItemStack applyTo(ItemStack input) {
        ItemStack stack = result.isEmpty() ? input.copyWithCount(
                result.getItem() == Items.AIR ? 1 : result.getCount()
            ) : result.copy();
        ItemFluids fluids = ItemFluids.of(input);
        Map<String, Integer> attributes = new HashMap<>(fluids.attributes());
        this.attributes.forEach((key, modder) -> {
            attributes.put(key, modder.applyAsInt(attributes.getOrDefault(key, 0).intValue()));
        });
        return ItemFluids.set(stack, ItemFluids.create(fluids.fluid(), fluids.amount(), attributes));
    }

    interface Op {
        int apply(int a, int b);
    }

    public enum Ops implements Op, StringIdentifiable {
        SET((a, b) -> b),
        ADD((a, b) -> a + b),
        SUBTRACT((a, b) -> a - b),
        MULTIPLY((a, b) -> a * b),
        DIVIDE((a, b) -> a / b);
        private static final Codec<Ops> CODEC = StringIdentifiable.createCodec(Ops::values);
        private static final PacketCodec<RegistryByteBuf, Ops> PACKET_CODEC = PacketCodecUtils.ofEnum(Ops.class);

        private final String name;
        private final Op operation;

        Ops(Op operation) {
            this.name = name().toLowerCase(Locale.ROOT);
            this.operation = operation;
        }

        @Override
        public int apply(int a, int b) {
            return operation.apply(a, b);
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public record Modification(int value, Ops type) implements Int2IntFunction {
        public static final Codec<Modification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("value").forGetter(Modification::value),
                Ops.CODEC.optionalFieldOf("type", Ops.ADD).forGetter(Modification::type)
        ).apply(instance, Modification::new));
        public static final PacketCodec<RegistryByteBuf, Modification> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, Modification::value,
                Ops.PACKET_CODEC, Modification::type,
                Modification::new
        );

        @Override
        public int get(int v) {
            return type.operation.apply(v, value);
        }
    }
}
