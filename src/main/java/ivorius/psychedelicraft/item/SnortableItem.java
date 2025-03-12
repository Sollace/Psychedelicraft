package ivorius.psychedelicraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;

/**
 * Created by lukas on 14.11.14.
 */
public class SnortableItem extends EdibleItem {
    public SnortableItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }
}
