package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public record AgeProperty() implements NumericProperty {
	public static final MapCodec<AgeProperty> CODEC = MapCodec.unit(new AgeProperty());

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holder, int seed) {
		return stack.getDamage() / 10F;
	}

	@Override
	public MapCodec<AgeProperty> getCodec() {
		return CODEC;
	}
}
