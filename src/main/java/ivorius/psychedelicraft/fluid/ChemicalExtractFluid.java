package ivorius.psychedelicraft.fluid;

import java.util.function.Consumer;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChemicalExtractFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 2);

    final Settings settings;

    public ChemicalExtractFluid(Identifier id, Settings settings) {
        super(id, settings.drinkable());
        this.settings = settings;
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);

        if (DISTILLATION.get(stack) >= 2) {
            consumer.accept(new DrugInfluence(DrugType.LSD, 3, 0.05, 0.003, 0.6F));
        }
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type, boolean openContainer) {
        int distillation = DISTILLATION.get(tank.getContents());
        if (type == ProcessType.DISTILL && distillation < 2) {
            return Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoFlowerExtract.ticksPerDistillation * (1 + distillation);
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, boolean openContainer) {
        MutableFluidContainer contents = tank.getContents();

        if (type == ProcessType.DISTILL) {
            int distillation = DISTILLATION.get(contents);
            MutableFluidContainer drained = contents.drain(2);
            DISTILLATION.set(drained, distillation + 1);
            return drained.withLevel(1).asStack();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public Text getName(ItemStack stack) {
        int distillation = DISTILLATION.get(stack);
        return Text.translatable(getTranslationKey() + ".distilled." + distillation, distillation);
    }

    @Override
    public int getColor(ItemStack stack) {
        return MathUtils.mixColors(
            super.getColor(stack),
            0xFFFFFFFF,
            DISTILLATION.get(stack) / 16F
        );
    }
}
