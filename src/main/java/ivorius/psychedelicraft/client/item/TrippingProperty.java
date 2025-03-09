package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public record TrippingProperty() implements BooleanProperty {
	public static final MapCodec<TrippingProperty> CODEC = MapCodec.unit(new TrippingProperty());

	@Override
	public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
		return DrugProperties.of(user).filter(DrugProperties::isTripping).isPresent();
	}

	@Override
	public MapCodec<TrippingProperty> getCodec() {
		return CODEC;
	}
}
