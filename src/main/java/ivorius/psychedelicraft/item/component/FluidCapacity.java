package ivorius.psychedelicraft.item.component;

import java.util.List;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.FluidTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;

public record FluidCapacity(int capacity) {
    private static final Interner<FluidCapacity> INTERNER = Interners.newStrongInterner();
    public static final FluidCapacity EMPTY = create(0);

    public static final Codec<FluidCapacity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("capacity").forGetter(FluidCapacity::capacity)
    ).apply(instance, FluidCapacity::create));
    public static final PacketCodec<RegistryByteBuf, FluidCapacity> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, FluidCapacity::capacity,
            FluidCapacity::create
    );

    public static FluidCapacity create(int capacity) {
        return INTERNER.intern(new FluidCapacity(capacity));
    }

    public static int get(ItemStack stack) {
        FluidCapacity capacity = stack.get(PSComponents.FLUID_CAPACITY);
        if (capacity == null) {
            return (int)FluidTransferUtils.getCapacity(stack);
        }
        return capacity == null ? 0 : capacity.capacity();
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (type.isAdvanced()) {
            tooltip.add(Text.translatable("psychedelicraft.container.levels", FluidVolumes.format(ItemFluids.of(stack).amount()), FluidVolumes.format(FluidCapacity.get(stack))));
        }
    }

    public static float getPercentage(ItemStack stack) {
        int capacity = get(stack);
        return capacity == 0 ? 0 : ItemFluids.of(stack).amount() / (float)capacity;
    }
}
