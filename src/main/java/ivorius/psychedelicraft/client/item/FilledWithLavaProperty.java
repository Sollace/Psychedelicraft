package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.tag.FluidTags;

public record FilledWithLavaProperty() implements BooleanProperty {
	public static final MapCodec<FilledWithLavaProperty> CODEC = MapCodec.unit(new FilledWithLavaProperty());

	@Override
	public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
        return ItemFluids.of(stack).isIn(FluidTags.LAVA);
	}

	@Override
	public MapCodec<FilledWithLavaProperty> getCodec() {
		return CODEC;
	}
}
