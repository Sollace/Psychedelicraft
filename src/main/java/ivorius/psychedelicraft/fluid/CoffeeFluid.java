/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;

/**
 * Created by lukas on 22.10.14.
 */
public class CoffeeFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> WARMTH = Attribute.ofInt("warmth", 0, 2);
    private static final IntProperty TEMPERATURE = IntProperty.of("temperature", 0, 2);

    public CoffeeFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    <O, S extends State<O, S>> void appendProperties(StateManager.Builder<O, S> builder) {
        builder.add(TEMPERATURE);
    }

    @Override
    <O, S extends State<O, S>> S copyState(State<?, ?> from, S to) {
        return to.withIfExists(TEMPERATURE, from.getOrEmpty(TEMPERATURE).orElse(0));
    }

    @Override
    public ItemStack getStack(State<?, ?> state, FluidContainer container) {
        return WARMTH.set(super.getStack(state, container), state.get(TEMPERATURE));
    }

    @Override
    public FluidState getFluidState(ItemStack stack) {
        return super.getFluidState(stack).with(TEMPERATURE, WARMTH.get(stack));
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);
        float warmth = (float)WARMTH.get(stack) / 2F;
        consumer.accept(new DrugInfluence(DrugType.CAFFEINE, 20, 0.002, 0.001, 0.25F + warmth * 0.05F));
        consumer.accept(new DrugInfluence(DrugType.WARMTH, 0, 0, 0.1, 0.8F * warmth));
    }

    @Override
    protected void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        int temperature = state.get(TEMPERATURE);
        if (temperature > 0 && world.getBlockState(pos).getBlock() instanceof FluidBlock) {
            world.setBlockState(pos, state.with(TEMPERATURE, temperature - 1).getBlockState());
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
    public int getProcessingTime(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
        return type == ProcessType.FERMENT && WARMTH.get(tank.getContents()) > 0 ? 300 : UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
        ItemStack contents = tank.getContents().drain(tank.getLevel()).asStack();
        return WARMTH.set(contents, Math.max(0, WARMTH.get(contents) - 1));
    }
}
