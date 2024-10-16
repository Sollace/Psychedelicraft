/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Optional;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.particle.BubbleParticleEffect;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

    private final Optional<Identifier> flowTexture;

    public SlurryFluid(Identifier id, Settings settings) {
        super(id, settings);
        this.flowTexture = Optional.of(getId().withPath(p -> "block/fluid/" + p + "_still"));
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        ParticleUtil.spawnParticle(world, pos, new BubbleParticleEffect(MathUtils.unpackRgbVector(getColor(ItemStack.EMPTY)), 1), ConstantIntProvider.create(5));

        world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS,
                0.5F + world.getRandom().nextFloat(),
                0.3F + world.getRandom().nextFloat(), true);
    }

    @Override
    public Optional<Identifier> getFlowTexture(ItemStack stack) {
        return flowTexture;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        if (type == ProcessType.FERMENT || type == ProcessType.MATURE) {
            return tank.getLevel() >= FLUID_PER_DIRT ? Psychedelicraft.getConfig().balancing.slurryHardeningTime : UNCONVERTABLE;
        }
        return UNCONVERTABLE;
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.FERMENT || type == ProcessType.MATURE) {
            MutableFluidContainer contents = tank.getContents();
            if (contents.getLevel() > FLUID_PER_DIRT) {
                contents.drain(FLUID_PER_DIRT);
                output.accept(Items.DIRT.getDefaultStack());
            }
        }
    }

    @Override
    public void getProcessStages(ProcessType type, ProcessStageConsumer consumer) {
        if (type == ProcessType.FERMENT || type == ProcessType.MATURE) {
            consumer.accept(Psychedelicraft.getConfig().balancing.slurryHardeningTime, 1,
                    stack -> List.of(getDefaultStack(FluidContainer.of(stack))),
                    stack -> List.of(Items.DIRT.getDefaultStack())
            );
        }
    }
}
