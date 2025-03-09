package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;

public record FluidTintSource(int defaultColor) implements TintSource {
    public static final MapCodec<FluidTintSource> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(Codecs.RGB.fieldOf("default").forGetter(FluidTintSource::defaultColor)).apply(instance, FluidTintSource::new)
    );

    @Override
    public int getTint(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user) {
        ItemFluids fluids = ItemFluids.of(stack);
        return fluids.isEmpty() ? defaultColor : FluidBoxRenderer.FluidAppearance.getItemColor(fluids);
    }

    @Override
    public MapCodec<FluidTintSource> getCodec() {
        return CODEC;
    }
}