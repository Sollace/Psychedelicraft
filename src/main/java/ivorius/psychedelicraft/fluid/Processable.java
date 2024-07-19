/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

/**
 * A fluid that can processed in the correct container, e.g. distillery.
 */
public interface Processable {
    /**
     * Tick value indicating that the fluid is in its most base form (cannot be converted to another type of fluid).
     */
    int UNCONVERTABLE = -1;
    /**
     * Returns the ticks needed for the fluid to pass through a particular conversion process.
     * Return {@link #UNCONVERTABLE} if the fluid cannot perform the requested conversion.
     *
     * @param stack The fluid currently being processed.
     * @return The time it needs to distill, in ticks.
     */
    int getProcessingTime(Resovoir tank, ProcessType type);

    /**
     * Notifies the fluid that the stack has distilled, and is expected to apply this change to the stack.
     *
     * @param stack The fluid currently being distilled.
     * @param output Consumer for byproducts of the conversion process
     */
    void process(Context context, ProcessType type, ByProductConsumer output);

    Stream<Process> getProcesses();

    default ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return type;
    }

    record Process(SimpleFluid fluid, Identifier id, List<Transition> transitions) {}

    record Transition(
            ProcessType type,
            int time,
            int multiplier,
            Function<ItemFluids, ItemFluids> input,
            Function<ItemFluids, ItemFluids> output
    ) {}

    interface ByProductConsumer {
        void accept(ItemStack stack);

        void accept(ItemFluids stack);
    }

    interface Context extends Inventory {
        default Resovoir getTankOnSide(Direction direction) {
            return getPrimaryTank();
        }

        default List<Resovoir> getAuxiliaryTanks() {
            return List.of();
        }

        default int getTotalFluidVolume() {
            return getAuxiliaryTanks().stream().mapToInt(r -> r.getContents().amount()).sum();
        }

        Resovoir getPrimaryTank();
    }

    enum ProcessType implements StringIdentifiable {
        /**
         * Nothing is happening, probably due to unmet requirements
         */
        IDLE,
        /**
         * When processed in a distillery, used to increase the purity (proof) of existing liquers.
         */
        DISTILL,
        /**
         * When processed in a barrel, used to age grape juice into wines of increasing quality.
         */
        MATURE,
        /**
         * When processed in a vat/mash tub, used to ferment sugars into alcohol
         */
        FERMENT,
        /**
         * When processed past its full fermentation in a vat/mash tub, starts producing acids instead of alcohols
         */
        ACETIFY,
        /**
         * When processed in the evaporator, used to chemically extract purified substances
         */
        PURIFY,
        /**
         * When fluids of differing types are mixed on a bunsen burner, used to change their properties when they combine.
         */
        REACT;

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Text status = Text.translatable("fluid.status." + name);
        private final String timeLabel = "time.until." + name;

        public static final Codec<ProcessType> CODEC = StringIdentifiable.createBasicCodec(ProcessType::values);
        public static final PacketCodec<RegistryByteBuf, ProcessType> PACKET_CODEC = PacketCodecUtils.ofEnum(ProcessType.class);

        public Text getStatus() {
            return status;
        }

        public String getTimeLabelTranslationKey() {
            return timeLabel;
        }

        @Override
        public String asString() {
            return name;
        }

        public static void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            Processable.ProcessType processType = stack.get(PSComponents.PROCESS_TYPE);
            if (processType != null) {
                tooltip.add(Text.translatable("psychedelicraft.container.process_type." + processType.asString()));
            }
        }
    }
}
