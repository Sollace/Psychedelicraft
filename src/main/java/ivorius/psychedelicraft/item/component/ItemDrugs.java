package ivorius.psychedelicraft.item.component;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public record ItemDrugs(List<DrugInfluence> influences) implements TooltipAppender {
    public static final ItemDrugs EMPTY = new ItemDrugs(List.of());
    public static final Codec<ItemDrugs> CODEC = RecordCodecBuilder.create(i -> i.group(
            DrugInfluence.CODEC.listOf().fieldOf("influences").forGetter(ItemDrugs::influences)
    ).apply(i, ItemDrugs::of));
    public static final PacketCodec<RegistryByteBuf, ItemDrugs> PACKET_CODEC = PacketCodec.tuple(
            DrugInfluence.PACKET_CODEC.collect(PacketCodecs.toList()), ItemDrugs::influences,
            ItemDrugs::of
    );

    public static ItemDrugs of(List<DrugInfluence> influences) {
        return new ItemDrugs(influences);
    }

    public static ItemDrugs of(DrugInfluence...influences) {
        return of(List.of(influences));
    }

    public ItemDrugs {
        influences = List.copyOf(influences);
    }

    public static ItemDrugs get(ItemStack stack) {
        return stack.getOrDefault(PSComponents.DRUGS, EMPTY);
    }

    public void applyTo(DrugProperties properties) {
        properties.addAll(influences);
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type, PlayerEntity playerEntity, ItemStack itemStack) {
        if (type.isAdvanced() && !influences.isEmpty()) {
            tooltip.accept(Text.translatable("psychedelicraft.item.contained_drug_effects").formatted(Formatting.GRAY));

            influences.forEach(influence -> {
                tooltip.accept(Text.translatable("psychedelicraft.item.contained_drug_effects.entry",
                        influence.getDrugType().id(),
                        Text.literal(String.format(Math.abs(influence.getTargetInfluence()) > MathHelper.EPSILON ? "%.2f" : "%.0f", influence.getTargetInfluence())),
                        Text.literal(String.format(Math.abs(influence.getBaseIncrease()) > MathHelper.EPSILON ? "%.3f" : "%.0f", influence.getBaseIncrease()))
                ).formatted(Formatting.DARK_PURPLE));
            });
        }
    }
}
