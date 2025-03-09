package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.item.SuspiciousItem;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public class HallucinatedItemModel implements ItemModel {
    public static final ItemModel INSTANCE = new HallucinatedItemModel();
	@Override
	public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ModelTransformationMode mode, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed) {
		if (stack.getItem() instanceof SuspiciousItem sus) {
            sus.getHallucinatedItem().map(Item::getDefaultStack).ifPresent(replacement -> {
                resolver.update(state, replacement, mode, world, user, seed);
            });
        }
	}

    public static record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            return INSTANCE;
        }

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}