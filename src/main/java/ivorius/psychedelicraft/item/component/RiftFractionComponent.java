package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.RiftJarItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record RiftFractionComponent(float amount) implements TooltipAppender {
    public static final RiftFractionComponent DEFAULT = new RiftFractionComponent(0F);
    public static final Codec<RiftFractionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(RiftFractionComponent::amount)
    ).apply(instance, RiftFractionComponent::new));
    public static final PacketCodec<RegistryByteBuf, RiftFractionComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, RiftFractionComponent::amount,
            RiftFractionComponent::new
    );

    public static float getRiftFraction(ItemStack stack) {
        RiftFractionComponent amount = stack.get(PSComponents.RIFT_FRACTION);
        return amount == null ? 0 : amount.amount();
    }

    public static ItemStack set(ItemStack stack, float riftFraction) {
        if (riftFraction > 0 && stack.getItem() instanceof RiftJarItem) {
            stack.set(PSComponents.RIFT_FRACTION, new RiftFractionComponent(riftFraction));
        }
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
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
