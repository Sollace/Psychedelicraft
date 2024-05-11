package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChemicalExtractFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 2);

    final Settings settings;
    private final DrugType drug;

    public ChemicalExtractFluid(Identifier id, Settings settings, DrugType drug) {
        super(id, settings.drinkable());
        this.settings = settings;
        this.drug = drug;
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);

        consumer.accept(new DrugInfluence(drug, 3, 0, 0.03, Math.pow(96F, DISTILLATION.get(stack))));
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
        int distillation = DISTILLATION.get(tank.getContents());
        if (type == ProcessType.DISTILL && distillation < 2) {
            return Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoFlowerExtract().ticksPerDistillation() * (1 + distillation);
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
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
    public void getProcessStages(ProcessType type, ProcessStageConsumer consumer) {
        if (type == ProcessType.DISTILL) {
            DISTILLATION.forEachStep((from, to) -> {
                consumer.accept(Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoFlowerExtract().ticksPerDistillation(),
                        1,
                        stack -> List.of(DISTILLATION.set(stack, from)),
                        stack -> List.of(DISTILLATION.set(stack, to))
                );
            });
        }
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

    @Override
    public int getHash(ItemStack stack) {
        return Objects.hash(this, DISTILLATION.get(stack));
    }
}
