/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;

/**
 * Created by lukas on 22.10.14.
 */
public class CoffeeFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> WARMTH = Attribute.ofInt("warmth", 0, 2);
    private static final FluidStateManager.FluidProperty<Integer> TEMPERATURE = new FluidStateManager.FluidProperty<>(IntProperty.of("temperature", 0, 2), WARMTH::set, WARMTH::get);

    public CoffeeFluid(Identifier id, Settings settings) {
        super(id, settings.with(TEMPERATURE));
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);
        float warmth = (float)WARMTH.get(stack) / 2F;
        consumer.accept(new DrugInfluence(DrugType.CAFFEINE, 20, 0.002, 0.001, 0.25F + warmth * 0.05F));
        consumer.accept(new DrugInfluence(DrugType.WARMTH, 0, 0, 0.1, 0.8F * warmth));
    }

    @Override
    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        int temperature = state.get(TEMPERATURE.property());
        if (temperature > 0 && world.getBlockState(pos).getBlock() instanceof FluidBlock) {
            world.setBlockState(pos, state.with(TEMPERATURE.property(), temperature - 1).getBlockState());
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey() + ".temperature." + WARMTH.get(stack));
    }

    @Override
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        super.getDefaultStacks(container, consumer);
        consumer.accept(WARMTH.set(getDefaultStack(container), 1));
        consumer.accept(WARMTH.set(getDefaultStack(container), 2));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSuitableContainer(FluidContainer container) {
        return container.asItem().getRegistryEntry().isIn(PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS);
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return type == ProcessType.FERMENT && WARMTH.get(tank.getContents()) > 0 ? 300 : UNCONVERTABLE;
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.FERMENT) {
            MutableFluidContainer contents = tank.getContents();
            WARMTH.set(contents, Math.max(1, WARMTH.get(contents) - 1));
        }
    }

    @Override
    public void getProcessStages(ProcessType type, ProcessStageConsumer consumer) {
        if (type == ProcessType.FERMENT) {
            consumer.accept(300, -1,
                    stack -> List.of(WARMTH.set(stack, 2)),
                    stack -> List.of(WARMTH.set(stack, 1))
            );
        }
    }

    @Override
    public int getHash(ItemStack stack) {
        return Objects.hash(this, WARMTH.get(stack));
    }
}
