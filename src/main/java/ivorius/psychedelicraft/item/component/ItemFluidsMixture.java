package ivorius.psychedelicraft.item.component;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import ivorius.psychedelicraft.util.compat.StackCompat;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public record ItemFluidsMixture(List<ItemFluids> fluids) implements TooltipAppender {
    private static final ItemFluidsMixture EMPTY = new ItemFluidsMixture(List.of());
    public static final Codec<ItemFluidsMixture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemFluids.CODEC.listOf().fieldOf("fluids").forGetter(ItemFluidsMixture::fluids)
    ).apply(instance, ItemFluidsMixture::of));
    public static final PacketCodec<PacketByteBuf, ItemFluidsMixture> PACKET_CODEC = PacketCodec.tuple(
            ItemFluids.PACKET_CODEC.collect(PacketCodecs.toList()), ItemFluidsMixture::fluids,
            ItemFluidsMixture::of
    );

    public static ItemFluidsMixture of(List<ItemFluids> fluids) {
        fluids = removeEmpty(fluids);
        return fluids.isEmpty() ? EMPTY : new ItemFluidsMixture(fluids);
    }

    @NotNull
    public static ItemFluidsMixture of(ItemStack stack) {
        ItemFluidsMixture fluids = StackCompat.get(stack, PSComponents.FLUIDS_MIXTURE);
        return fluids == null ? EMPTY : fluids;
    }

    public static ItemStack set(ItemStack stack, List<ItemFluids> fluids) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            ItemFluidsMixture mixture = of(fluids);
            if (mixture.fluids.size() < 2) {
                StackCompat.remove(stack, PSComponents.FLUIDS_MIXTURE);
                return ItemFluids.set(stack, mixture.getFirstFluid());
            }
            stack = ItemFluids.getItemForFluids(stack, mixture.getFirstFluid());
            StackCompat.set(stack, PSComponents.FLUIDS_MIXTURE, mixture);
        }
        return stack;
    }

    private static List<ItemFluids> removeEmpty(List<ItemFluids> fluids) {
        return fluids.stream().filter(f -> !f.isEmpty()).toList();
    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public ItemFluids getFirstFluid() {
        return isEmpty() ? ItemFluids.EMPTY : fluids.get(0);
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip) {
        if (isEmpty()) {
            return;
        }
        int total = fluids().stream().mapToInt(ItemFluids::amount).sum();
        tooltip.accept(Text.translatable("psychedelicraft.container.mixture").formatted(Formatting.DARK_GRAY));
        fluids().forEach(fluid -> {
            tooltip.accept(Text.translatable("psychedelicraft.container.mixture.fluid", getPercentage(fluid.amount(), total), fluid.getName()));
        });
    }

    static int getPercentage(int amount, int total) {
        return MathHelper.floor(100 * ((float)amount / total));
    }
}
