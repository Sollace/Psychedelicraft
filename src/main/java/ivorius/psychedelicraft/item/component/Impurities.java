package ivorius.psychedelicraft.item.component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.PacketCodecUtils;
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
    public static final Impurities EMPTY = new Impurities(Set.of());

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
        return set(stack, Set.of(impurities));
    }

    public static ItemStack set(ItemStack stack, Set<Impurity> impurities) {
        stack.set(PSComponents.IMPURITIES, new Impurities(impurities));
        return stack;
    }

    public static ItemStack set(ItemStack stack, Impurities impurities) {
        stack.set(PSComponents.IMPURITIES, impurities);
        return stack;
    }

    public static Impurities combine(Impurities a, Impurities b) {
        Set<Impurity> combined = new HashSet<>(a.impurities());
        combined.addAll(b.impurities());
        return new Impurities(combined);
    }

    public static Impurities overlap(Impurities a, Impurities b) {
        Set<Impurity> combined = new HashSet<>();
        for (Impurity i : a.impurities()) {
            if (b.impurities().contains(i)) {
                combined.add(i);
            }
        }
        return new Impurities(combined);
    }

    public float getEffectStrengthModifier() {
        float strength = 1;
        if (impurities.contains(Impurity.CARBON)) {
            strength /= 2F;
        }
        if (impurities.contains(Impurity.PETROLIUM)) {
            strength *= 1.5F;
        }
        if (impurities.contains(Impurity.ETHANOL)) {
            strength *= 1.5F;
        }
        if (impurities.contains(Impurity.SILICA)) {
            strength /= 2.5F;
        }
        return strength;
    }

    public float getEffectDelayModifier() {
        float strength = 1;
        if (impurities.contains(Impurity.GASOLINE)) {
            strength /= 2F;
        }
        if (impurities.contains(Impurity.PETROLIUM)) {
            strength /= 2F;
        }
        return strength;
    }

    public List<DrugInfluence> modifyEffects(List<DrugInfluence> influences) {
        if (impurities.isEmpty()) {
            return influences;
        }

        float strength = getEffectStrengthModifier();
        float delayModifier = getEffectDelayModifier();

        Stream<DrugInfluence> stream = influences.stream();

        if (impurities.contains(Impurity.ETHANOL)) {
            stream = Stream.concat(Stream.of(
                new DrugInfluence(DrugType.ALCOHOL, DrugInfluence.DelayType.METABOLISED, 0.1F, 1, 0.8F)
            ), stream);
        }
        return stream.map(i -> {
            return i.copyWithMaximum(i.getTargetInfluence() * strength).copyWithDelay(Math.max(1, (int)(i.getDelay() * delayModifier)));
        }).toList();
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (!impurities.isEmpty()) {
            tooltip.accept(impurities.stream().map(i -> i.getName())
                .reduce(null, (a, b) -> a == null ? b : b == null ? a : a.copy().append(", ").append(b)));
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
