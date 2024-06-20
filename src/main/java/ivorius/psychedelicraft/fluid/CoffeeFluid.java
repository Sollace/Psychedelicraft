/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.item.component.ItemFluids;

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
    protected void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
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
    public Text getName(ItemFluids stack) {
        return Text.translatable(getTranslationKey() + ".temperature." + WARMTH.get(stack));
    }


    @Override
    public Stream<ItemFluids> getDefaultStacks(int capacity) {
        return WARMTH.steps().map(Pair::getRight)
                .map(warmth -> WARMTH.set(getDefaultStack(capacity), warmth));
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.isIn(getPreferredContainerTag());
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return type == ProcessType.FERMENT && WARMTH.get(tank.getContents()) > 0 ? 300 : UNCONVERTABLE;
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.FERMENT) {
            tank.setContents(WARMTH.set(tank.getContents(), Math.max(0, WARMTH.get(tank.getContents()) - 1)));
        }
    }

    @Override
    public <T> Stream<T> getProcessStages(ProcessType type, ProcessStageConsumer<T> consumer) {
        if (type == ProcessType.FERMENT) {
            return WARMTH.steps().map(step -> consumer.accept(300, -1,
                    stack -> WARMTH.set(stack, step.getRight()),
                    stack -> WARMTH.set(stack, step.getLeft())
            ));
        }

        return Stream.empty();
    }

    @Override
    public int getHash(ItemFluids stack) {
        return Objects.hash(this, WARMTH.get(stack));
    }
}
