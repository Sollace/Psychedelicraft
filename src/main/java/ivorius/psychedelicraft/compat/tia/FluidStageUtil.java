package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.ItemFluids;

public interface FluidStageUtil {

    static <T> Stream<T> combineSimilarProcesses(Stream<Processable.Process> processes, Function<Processable.Process, T> unit, Function<Set<Processable.Process>, T> reducer) {
        return groupBy(processes.filter(p -> !p.transitions().isEmpty()), i -> i.transitions().size()).flatMap(group -> {
            if (group.getValue().size() <= 1) {
                return group.getValue().stream().map(unit);
            }

            return groupBy(group.getValue().stream(), p -> p.transitions().stream().map(t -> new TransitionKey(p, t)).toList()).map(similars -> {
                return reducer.apply(similars.getValue());
            });
        });
    }


    private static <K, T> Stream<Map.Entry<K, Set<T>>> groupBy(Stream<T> stream, Function<T, K> keyMapper) {
        Map<K, Set<T>> groups = new HashMap<>();
        stream.forEach(t -> {
            groups.computeIfAbsent(keyMapper.apply(t), k -> new HashSet<>()).add(t);
        });
        return groups.entrySet().stream();
    }

    record TransitionKey(Processable.ProcessType type, int time, int multiplier, ItemFluids result) {
        TransitionKey(Processable.Process process, Processable.Transition transition) {
            this(transition.type(), transition.time(), transition.multiplier(), transition.output().apply(process.fluid().getDefaultStack()));
        }
    }
}
