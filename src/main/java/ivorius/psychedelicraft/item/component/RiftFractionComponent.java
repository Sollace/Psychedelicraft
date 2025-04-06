package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.RiftJarItem;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import ivorius.psychedelicraft.util.compat.StackCompat;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record RiftFractionComponent(float amount) implements TooltipAppender {
    public static final RiftFractionComponent DEFAULT = new RiftFractionComponent(0F);
    public static final Codec<RiftFractionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(RiftFractionComponent::amount)
    ).apply(instance, RiftFractionComponent::new));
    public static final PacketCodec<PacketByteBuf, RiftFractionComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, RiftFractionComponent::amount,
            RiftFractionComponent::new
    );

    public static float getRiftFraction(ItemStack stack) {
        RiftFractionComponent amount = StackCompat.get(stack, PSComponents.RIFT_FRACTION);
        return amount == null ? 0 : amount.amount();
    }

    public static ItemStack set(ItemStack stack, float riftFraction) {
        if (riftFraction > 0 && stack.getItem() instanceof RiftJarItem) {
            StackCompat.set(stack, PSComponents.RIFT_FRACTION, new RiftFractionComponent(riftFraction));
        }
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip) {
        tooltip.accept(Text.translatable("item.psychedelicraft.rift_jar." + getUnlocalizedFractionName(amount)).formatted(Formatting.GRAY));
    }

    private static String getUnlocalizedFractionName(float fraction) {
        if (fraction <= 0) {
            return "empty";
        }
        if (fraction < 0.4F) {
            return "slightly_filled";
        }
        if (fraction < 0.6F) {
            return "half_filled";
        }
        if (fraction < 0.8F) {
            return "filled";
        }

        return "over_filled";
    }
}
