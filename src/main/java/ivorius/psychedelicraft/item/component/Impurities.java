package ivorius.psychedelicraft.item.component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

public record Impurities(Set<Impurity> impurities) implements TooltipAppender {
    private static final Impurities EMPTY = new Impurities(Set.of());

    public static final Codec<Impurities> CODEC = RecordCodecBuilder.create(i -> i.group(
            Impurity.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("impurities").forGetter(Impurities::impurities)
    ).apply(i, Impurities::new));
    public static final PacketCodec<RegistryByteBuf, Impurities> PACKET_CODEC = PacketCodecUtils.ofEnum(Impurity.class)
            .collect(PacketCodecs.toCollection(i -> (Set<Impurity>)new HashSet<Impurity>(i)))
            .xmap(Impurities::new, Impurities::impurities);

    public Impurities {
        impurities = impurities.isEmpty() ? EnumSet.noneOf(Impurity.class) : EnumSet.copyOf(impurities);
    }

    public static Impurities get(ItemStack stack) {
        return stack.getOrDefault(PSComponents.IMPURITIES, EMPTY);
    }

    public static boolean isOn(ItemStack stack, Impurity impurity) {
        return get(stack).impurities().contains(impurity);
    }

    public static ItemStack set(ItemStack stack, Impurity...impurities) {
        stack.set(PSComponents.IMPURITIES, new Impurities(Set.of(impurities)));
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type, PlayerEntity playerEntity, ItemStack itemStack) {

        if (!impurities.isEmpty()) {
            impurities.stream().map(i -> i.getName())
                .reduce(null, (a, b) -> a == null ? b : b == null ? a : a.copy().append(", ").append(b));
        }
    }

    public enum Impurity implements StringIdentifiable {
        CARBON,
        ETHANOL,
        PETROLIUM,
        GASOLINE,
        SILICA;

        public static final Codec<Impurity> CODEC = StringIdentifiable.createCodec(Impurity::values);

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Text displayName = Text.translatable(Util.createTranslationKey("impurity", Psychedelicraft.id(name)));

        @Override
        public String asString() {
            return name;
        }

        public Text getName() {
            return displayName;
        }
    }

}
