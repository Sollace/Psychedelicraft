package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.entity.MolotovCocktailEntity;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public record FlyingProperty() implements BooleanProperty {
	public static final MapCodec<FlyingProperty> CODEC = MapCodec.unit(new FlyingProperty());

	@Override
	public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
		return stack.getHolder() instanceof MolotovCocktailEntity;
	}

	@Override
	public MapCodec<FlyingProperty> getCodec() {
		return CODEC;
	}
}
