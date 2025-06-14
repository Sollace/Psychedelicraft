package ivorius.psychedelicraft.fluid;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DelayType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.alcohol.TickRate;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChemicalExtractFluid extends DrugFluid implements Processable, TickRate.Tickable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 2);

    final Settings settings;
    private final DrugType<?> drug;

    private final DrugFluid purifiedForm;
    private static final TickRate DEFAULT_TICK_RATE = TickRate.ofMinutes(40, 1, 30, 30);

    public ChemicalExtractFluid(Identifier id, Settings settings, DrugType<?> drug, DrugFluid purifiedForm) {
        super(id, settings.drinkable());
        this.settings = settings;
        this.drug = drug;
        this.purifiedForm = purifiedForm;
    }

    @Override
    protected void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);

        consumer.accept(new DrugInfluence(drug, DelayType.IMMEDIATE, 0.03, 0, Math.pow(96F, DISTILLATION.get(stack))));
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        if (type == ProcessType.PURIFY) {
            int distillation = DISTILLATION.get(tank.getContents());
            return getTickRate().ticksPerDistillation() * (1 + distillation);
        }

        return UNCONVERTABLE;
    }

    @Override
    public TickRate getDefaultTickRate() {
        return DEFAULT_TICK_RATE;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.PURIFY) {
            Resovoir tank = context.getPrimaryTank();
            int amount = Math.max(1, tank.getContents().amount() / 10);
            ItemFluids fluids = tank.drain(amount);
            if (DISTILLATION.get(fluids) < 2) {
                output.accept(DISTILLATION.cycle(fluids));
            } else {
                output.accept(purifiedForm.getDefaultStack(1).ofAmount(amount));
            }
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        int distillRate = getTickRate().ticksPerDistillation();

        return Stream.of(
                new Process(this, getId().withSuffixedPath("_reducing"), DISTILLATION.steps().map(step -> {
                    return new Transition(ProcessType.PURIFY, distillRate * (1 + step.getLeft()),
                            1,
                            from -> DISTILLATION.set(from, step.getLeft()),
                            to -> DISTILLATION.set(to, step.getRight())
                    );
                }).toList()),
                new Process(this, getId().withSuffixedPath("_purifying"), DISTILLATION.steps().map(step -> {
                    return new Transition(ProcessType.PURIFY, distillRate * 3,
                            1,
                            from -> DISTILLATION.set(from, 2).ofAmount(2),
                            to -> purifiedForm.getDefaultStack(1)
                    );
                }).toList())
        );
    }

    @Override
    public Text getName(ItemFluids stack) {
        int distillation = DISTILLATION.get(stack);
        return Text.translatable(getTranslationKey() + ".distilled." + distillation, distillation);
    }

    @Override
    public int getColor(ItemFluids stack) {
        return MathUtils.mixColors(
            super.getColor(stack),
            0xFFFFFFFF,
            DISTILLATION.get(stack) / 16F
        );
    }

    @Override
    public int getHash(ItemFluids stack) {
        return Objects.hash(this, DISTILLATION.get(stack));
    }
}
