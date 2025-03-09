package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public record UsingProperty() implements BooleanProperty {
	public static final MapCodec<UsingProperty> CODEC = MapCodec.unit(new UsingProperty());

	@Override
	public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
        return user != null && user.getActiveItem() == stack && user.getItemUseTimeLeft() > 0;
	}

	@Override
	public MapCodec<UsingProperty> getCodec() {
		return CODEC;
	}
}
