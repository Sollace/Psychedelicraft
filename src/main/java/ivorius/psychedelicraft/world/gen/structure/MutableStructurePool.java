package ivorius.psychedelicraft.world.gen.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.registry.Registry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.Identifier;

public interface MutableStructurePool {
    static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            Registry<StructurePool> structurePools = registries.get(Registry.STRUCTURE_POOL_KEY);
            Map<Identifier, PoolPair> registeredPools = new HashMap<>();
            RegistryEntryAddedCallback.event(structurePools).register((rawId, id, pool) -> {
                boolean isInjectedPool = id.getNamespace().equals("psychedelicraftmc");
                if (isInjectedPool || id.getNamespace().equals("minecraft")) {
                    Identifier targetId = isInjectedPool ? new Identifier(id.getPath()) : id;

                    if (registeredPools.computeIfAbsent(targetId, PoolPair::new).offer(isInjectedPool, pool)) {
                        registeredPools.remove(targetId);
                    }
                }
            });
        });
    }

    static MutableStructurePool of(StructurePool pool) {
        return (MutableStructurePool)pool;
    }

    ObjectArrayList<StructurePoolElement> getElements();

    void setElements(ObjectArrayList<StructurePoolElement> elements);

    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementCounts);

    class PoolPair {
        @Nullable
        private MutableStructurePool source;
        @Nullable
        private MutableStructurePool target;

        PoolPair(Identifier id) {}

        public boolean offer(boolean isSource, StructurePool pool) {
            if (isSource) {
                source = of(pool);
            } else {
                target = of(pool);
            }
            if (source != null && target != null) {
                ObjectArrayList<StructurePoolElement> elements = new ObjectArrayList<>(target.getElements());
                elements.addAll(source.getElements());
                target.setElements(elements);

                List<Pair<StructurePoolElement, Integer>> elementCounts = new ArrayList<>(target.getElementCounts());
                elementCounts.addAll(source.getElementCounts());
                target.setElementCounts(elementCounts);
                return true;
            }
            return false;
        }
    }
}
