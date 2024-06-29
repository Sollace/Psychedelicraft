/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.util.*;

import java.util.List;
import java.util.stream.Stream;

import ivorius.psychedelicraft.fluid.container.Resovoir;

/**
 * Created by lukas on 22.10.14.
 */
public class CocaTeaFluid extends DrugFluid implements Processable {
    public CocaTeaFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return type != ProcessType.PURIFY ? Processable.UNCONVERTABLE : 1;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.PURIFY) {
            context.getPrimaryTank().drain(2);
            output.accept(PSFluids.COCAINE.getDefaultStack(1));
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.of(new Process(this, getId().withSuffixedPath("_purified"), List.of(
            new Transition(ProcessType.PURIFY, 0, 1, from -> from.ofAmount(2), to -> PSFluids.COCAINE.getDefaultStack(1))
        )));
    }
}
