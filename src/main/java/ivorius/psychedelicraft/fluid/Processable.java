/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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
    void process(Resovoir tank, ProcessType type, ByProductConsumer output);

    void getProcessStages(ProcessType type, ProcessStageConsumer consumer);

    default ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return type;
    }

    interface ProcessStageConsumer {
        void accept(int time, int change,
                Function<ItemStack, List<ItemStack>> from,
                Function<ItemStack, List<ItemStack>> to
        );
    }

    interface ByProductConsumer {
        void accept(ItemStack stack);

        void accept(MutableFluidContainer fluid);
    }

    enum ProcessType {
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
         * When fluids of differing types are mixed in a flask, used to change their properties when they combine.
         */
        REACT;

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Text status = Text.translatable("fluid.status." + name);
        private final String timeLabel = "time.until." + name;

        public Text getStatus() {
            return status;
        }

        public String getTimeLabelTranslationKey() {
            return timeLabel;
        }
    }
}
