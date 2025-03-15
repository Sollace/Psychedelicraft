package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.util.Identifier;

public class PetroliumFluid extends SimpleFluid implements Processable {

    public PetroliumFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return type == ProcessType.DISTILL ? 1000 : Processable.UNCONVERTABLE;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.DISTILL) {
            if (!context.getPrimaryTank().drain(1).isEmpty()) {
                output.accept(PSFluids.GASOLINE.getDefaultStack());
            }
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.of(new Process(this, Psychedelicraft.id("refine"), List.of(
                new Transition(ProcessType.DISTILL, 1000, 1, Function.identity(), f -> PSFluids.GASOLINE.getDefaultStack(f.amount()))
        )));
    }
}
