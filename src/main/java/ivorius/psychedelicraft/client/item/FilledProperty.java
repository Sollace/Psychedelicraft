package ivorius.psychedelicraft.client.item;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.item.BongItem;
import ivorius.psychedelicraft.item.PaperBagItem;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.StringIdentifiable;

public record FilledProperty() implements SelectProperty<FilledProperty.FillPercentage> {
	public static final SelectProperty.Type<FilledProperty, FillPercentage> TYPE = SelectProperty.Type.create(MapCodec.unit(new FilledProperty()), FillPercentage.CODEC);

	@Override
	public FillPercentage getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
	    if (stack.getItem() instanceof PaperBagItem) {
            BagContentsComponent contents = BagContentsComponent.get(stack);
            return contents.isEmpty() ? FillPercentage.EMPTY : contents.count() > BagContentsComponent.FULL_COUNT
                    ? (contents.count() > BagContentsComponent.FULL_COUNT * 2F ? FillPercentage.FULL : FillPercentage.THREE_QUARTER) : FillPercentage.HALF;
        }
        if (stack.getItem() instanceof BongItem item) {
            return item.hasUsableConsumable(user) ? FillPercentage.FULL : FillPercentage.EMPTY;
        }
        return ItemFluids.of(stack).isEmpty() ? FillPercentage.EMPTY : FillPercentage.FULL;
	}

	@Override
	public SelectProperty.Type<FilledProperty, FillPercentage> getType() {
		return TYPE;
	}

	public enum FillPercentage implements StringIdentifiable {
	    EMPTY,
	    ONE_QUARTER,
	    HALF,
	    THREE_QUARTER,
	    FULL;

	    public static final Codec<FillPercentage> CODEC = StringIdentifiable.createCodec(FillPercentage::values);

	    private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return name;
        }
	}
}
