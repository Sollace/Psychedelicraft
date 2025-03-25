package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

public class VanillaFluid extends SimpleFluid implements ConsumableFluid {
    static SimpleFluid register(Identifier id, Fluid fluid, boolean empty) {
        return Registry.register(SimpleFluid.REGISTRY, id, new VanillaFluid(id, fluid, empty));
    }

    static {
        Registries.FLUID.streamEntries().forEach(entry -> {
            register(entry.getKey().get().getValue(), entry.value());
        });
        RegistryEntryAddedCallback.event(Registries.FLUID).register((rawId, id, value) -> register(id, value));
    }

    private static void register(Identifier id, Fluid value) {
        if (VanillaFluid.toStill(value) == value && !REGISTRY.containsId(id) && !"minecraft:empty".equals(id.toString())) {
            VanillaFluid.register(id, value, false);
            Psychedelicraft.LOGGER.info("Added vanilla fluid " + id);
        }
    }

    private VanillaFluid(Identifier id, Fluid still, boolean empty) {
        super(id, 0xFFFFFFFF,
                new PhysicalFluid(still, toFlowing(still), still.getDefaultState().getBlockState().getBlock()),
                empty
        );
    }

    private static Fluid toFlowing(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getFlowing() : fluid;
    }

    static Fluid toStill(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getStill() : fluid;
    }

    @Override
    public boolean canConsume(ItemStack stack, LivingEntity entity, ConsumptionType type) {
        return getPhysical().isIn(FluidTags.LAVA) && type == ConsumptionType.INJECT;
    }

    @Override
    public void consume(ItemFluids stack, LivingEntity entity, ConsumptionType type) {
        if (getPhysical().isIn(FluidTags.LAVA)) {
            entity.setOnFireFromLava();
            entity.setOnFireFor(30F);
        }
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.isIn(getPreferredContainerTag()) || container.isOf(Items.BUCKET) || container.isOf(PSItems.FILLED_BUCKET);
    }
}
