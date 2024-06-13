package ivorius.psychedelicraft.item.component;

import java.util.function.UnaryOperator;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSComponents {
    ComponentType<BagContentsComponent> BAG_CONTENTS = register("bag_contents", builder -> builder.codec(BagContentsComponent.CODEC).packetCodec(BagContentsComponent.PACKET_CODEC));
    ComponentType<RiftFractionComponent> RIFT_FRACTION = register("rift_fraction", builder -> builder.codec(RiftFractionComponent.CODEC).packetCodec(RiftFractionComponent.PACKET_CODEC));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Psychedelicraft.id(id), builderOperator.apply(ComponentType.builder()).build());
    }

    static void bootstrap() {
    }
}
