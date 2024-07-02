package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EthanolFluid extends DrugFluid {
    public EthanolFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public boolean hasRandomTicks() {
        return true;
    }

    @Override
    public float getFireStrength(ItemFluids fluidStack) {
        return 1;
    }

    @Override
    public float getExplosionStrength(ItemFluids fluidStack) {
        return 1;
    }

    @Override
    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        world.getOtherEntities(null, Box.of(pos.toCenterPos(), 5, 5, 5)).forEach(entity -> {
            if (random.nextInt(30) == 0) {
                DrugProperties.of(entity).ifPresentOrElse(properties -> {
                    properties.addToDrug(DrugType.ALCOHOL, 0.1);
                }, () -> {
                    entity.damage(world.getDamageSources().create(PSDamageTypes.ALCOHOL_POSIONING), 1);
                });
            }
        });
    }
}
