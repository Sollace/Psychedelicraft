package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record ContainedFluidProperty(TagKey<Fluid> fluid) implements BooleanProperty {
	public static final MapCodec<ContainedFluidProperty> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
	        TagKey.codec(RegistryKeys.FLUID).fieldOf("fluid").forGetter(ContainedFluidProperty::fluid)
    ).apply(i, ContainedFluidProperty::new));

	@Override
	public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
        return ItemFluids.of(stack).isIn(fluid);
	}

	@Override
	public MapCodec<ContainedFluidProperty> getCodec() {
		return CODEC;
	}
}
