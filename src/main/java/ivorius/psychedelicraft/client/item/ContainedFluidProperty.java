package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record ContainedFluidProperty(SimpleFluid fluid) implements BooleanProperty {
	public static final MapCodec<ContainedFluidProperty> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
	        SimpleFluid.CODEC.fieldOf("fluid").forGetter(ContainedFluidProperty::fluid)
    ).apply(i, ContainedFluidProperty::new));

	@Override
	public boolean test(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return ItemFluids.of(stack).isOf(fluid);
	}

	@Override
	public MapCodec<ContainedFluidProperty> getCodec() {
		return CODEC;
	}
}
