package ivorius.psychedelicraft.fluid;

import java.util.function.Function;

import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class VanillaFluid extends SimpleFluid implements ConsumableFluid {
    static final Function<Fluid, SimpleFluid> LOOKUP = ((Function<Fluid, Fluid>)(VanillaFluid::toStill)).andThen(Util.memoize(VanillaFluid::new));

    @SuppressWarnings("deprecation")
    private VanillaFluid(Fluid still) {
        this(still.getRegistryEntry().getKey().get().getValue(), still, false);
    }

    VanillaFluid(Identifier id, Fluid still, boolean empty) {
        super(id, 0xFFFFFFFF,
                new PhysicalFluid(still, toFlowing(still), still.getDefaultState().getBlockState().getBlock()),
                empty
        );
    }

    private static Fluid toFlowing(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getFlowing() : fluid;
    }

    private static Fluid toStill(Fluid fluid) {
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
