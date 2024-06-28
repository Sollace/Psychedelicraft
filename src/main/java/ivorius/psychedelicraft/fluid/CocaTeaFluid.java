/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.util.*;
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
        return type != ProcessType.REACT ? Processable.UNCONVERTABLE : 1;
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.REACT) {
            tank.drain(2);
            output.accept(PSFluids.COCAINE.getDefaultStack(1));
        }
    }

    @Override
    public <T> Stream<T> getProcessStages(ProcessType type, ProcessStageConsumer<T> consumer) {
        if (type == ProcessType.REACT) {
            return Stream.of(consumer.accept(0, 1, from -> from.ofAmount(2), to -> PSFluids.COCAINE.getDefaultStack(1)));
        }
        return Stream.empty();
    }

}
