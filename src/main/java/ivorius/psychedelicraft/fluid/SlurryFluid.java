/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.Optional;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

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
    public Optional<Identifier> getFlowTexture(ItemStack stack) {
        return flowTexture;
    }

    @Override
    public int getProcessingTime(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.FERMENT) {
            return stack.getCount() >= FLUID_PER_DIRT ? Psychedelicraft.getConfig().balancing.slurryHardeningTime : UNCONVERTABLE;
        }
        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, boolean openContainer) {
        if (type == ProcessType.FERMENT) {
            return new ItemStack(Items.DIRT, tank.getStack().getCount() / FLUID_PER_DIRT);
        }
        return ItemStack.EMPTY;
    }
}
