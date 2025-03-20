/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Created by lukas on 27.10.14.
 */
public class SlurryFluid extends SimpleFluid implements Processable {
    public static final int FLUID_PER_DIRT = FluidVolumes.BUCKET * 4;

    public SlurryFluid(Identifier id, Settings settings) {
        super(id, settings.sprites(
                id.withPath(p -> "block/fluid/" + p + "_flow"),
                id.withPath(p -> "block/fluid/" + p + "_still")
        ));
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        ParticleUtil.spawnParticle(world, pos, new DrugDustParticleEffect(PSParticles.BUBBLE, MathUtils.unpackRgb(getColor(getDefaultStack())), 1), ConstantIntProvider.create(5));

        world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS,
                0.5F + world.getRandom().nextFloat(),
                0.3F + world.getRandom().nextFloat(), true);
    }

    @Override
    public ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return type == ProcessType.FERMENT || type == ProcessType.MATURE ? ProcessType.SEPARATE : type;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        if (type == ProcessType.SEPARATE) {
            return tank.getContents().amount() >= FLUID_PER_DIRT ? Psychedelicraft.getConfig().slurryHardeningTime.get() : UNCONVERTABLE;
        }
        return UNCONVERTABLE;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.SEPARATE) {
            Resovoir tank = context.getPrimaryTank();
            if (tank.getContents().amount() >= FLUID_PER_DIRT) {
                tank.drain(FLUID_PER_DIRT);
                output.accept(Items.MUD.getDefaultStack());
                output.accept(SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.BUCKET * 3));
            }
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.empty();
    }

    @Override
    public void appendTankTooltip(ItemFluids stack, @Nullable World world, List<Text> tooltip, FluidProcessingBlockEntity tank) {
        super.appendTankTooltip(stack, world, tooltip, tank);

        if (tank.getProcessType() == ProcessType.DISTILL || tank.getProcessType() == ProcessType.FERMENT) {
            tooltip.add(Text.literal("Requirements:"));
            if (tank.getPrimaryTank().getAmount() < FLUID_PER_DIRT) {
                tooltip.add(Text.translatable("* Requires 4 buckets of slurry to process").formatted(Formatting.RED, Formatting.ITALIC));
            } else {
                tooltip.add(Text.translatable("* Has 4 buckets of slurry").formatted(Formatting.GRAY));
            }
        }
    }
}
