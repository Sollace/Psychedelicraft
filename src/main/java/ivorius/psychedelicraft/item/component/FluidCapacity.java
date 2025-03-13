package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.FluidTransferUtils;
import ivorius.psychedelicraft.fluid.container.RecepticalHandler;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.component.ComponentType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

            int maxLevel = (int)FluidTransferUtils.getCapacity(stack);

            if (maxLevel == 0) {
                ItemStack filledStack = RecepticalHandler.get(stack).toFilled(stack, ItemFluids.of(FluidVariant.of(Fluids.WATER), 1));
                if (filledStack != stack && filledStack.getItem() != stack.getItem()) {
                    capacity = filledStack.get(PSComponents.FLUID_CAPACITY);
                    if (capacity != null) {
                        return capacity.capacity();
                    }
                }
            }

            return maxLevel;
        }
        return capacity == null ? 0 : capacity.capacity();
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        tooltip.accept(Text.translatable("psychedelicraft.container.levels",
                FluidVolumes.format(ItemFluids.of(stack).amount()),
                FluidVolumes.format(FluidCapacity.get(stack))
        ).formatted(Formatting.DARK_PURPLE));
    }

    public static float getPercentage(ItemStack stack) {
        int capacity = get(stack);
        return capacity == 0 ? 0 : ItemFluids.of(stack).amount() / (float)capacity;
    }

    public record Predicate(IntRange capacity) implements ComponentSubPredicate<FluidCapacity> {
        public static final Codec<Predicate> CODEC = IntRange.CODEC.xmap(Predicate::new, Predicate::capacity);

        @Override
        public ComponentType<FluidCapacity> getComponentType() {
            return PSComponents.FLUID_CAPACITY;
        }

        @Override
        public boolean test(ItemStack stack, FluidCapacity capacity) {
            return this.capacity.test(capacity.capacity());
        }
    }

}
