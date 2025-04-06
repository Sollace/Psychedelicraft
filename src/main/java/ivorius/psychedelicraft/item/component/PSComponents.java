package ivorius.psychedelicraft.item.component;

import java.util.function.UnaryOperator;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.util.compat.ComponentType;

public interface PSComponents {
    ComponentType<BagContentsComponent> BAG_CONTENTS = register("bag_contents", builder -> builder.codec(BagContentsComponent.CODEC).packetCodec(BagContentsComponent.PACKET_CODEC));
    ComponentType<RiftFractionComponent> RIFT_FRACTION = register("rift_fraction", builder -> builder.codec(RiftFractionComponent.CODEC).packetCodec(RiftFractionComponent.PACKET_CODEC));
    ComponentType<ItemFluids> FLUIDS = register("fluids", builder -> builder.codec(ItemFluids.CODEC).packetCodec(ItemFluids.PACKET_CODEC));
    ComponentType<ItemFluidsMixture> FLUIDS_MIXTURE = register("fluids_mixture", builder -> builder.codec(ItemFluidsMixture.CODEC).packetCodec(ItemFluidsMixture.PACKET_CODEC));
    ComponentType<FluidCapacity> FLUID_CAPACITY = register("fluid_capacity", builder -> builder.codec(FluidCapacity.CODEC).packetCodec(FluidCapacity.PACKET_CODEC));
    ComponentType<Processable.ProcessType> PROCESS_TYPE = register("process_type", builder -> builder.codec(Processable.ProcessType.CODEC).packetCodec(Processable.ProcessType.PACKET_CODEC));
    ComponentType<ItemDrugs> DRUGS = register("drugs", builder -> builder.codec(ItemDrugs.CODEC).packetCodec(ItemDrugs.PACKET_CODEC));
    ComponentType<Impurities> IMPURITIES = register("impurities", builder -> builder.codec(Impurities.CODEC).packetCodec(Impurities.PACKET_CODEC));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return ComponentType.register(Psychedelicraft.id(id), builderOperator.apply(new ComponentType.Builder<>()));
    }

    static void bootstrap() {
        PSSubPredicates.bootstrap();
    }
}
