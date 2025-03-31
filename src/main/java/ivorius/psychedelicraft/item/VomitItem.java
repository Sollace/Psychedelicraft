package ivorius.psychedelicraft.item;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;

public class VomitItem extends Item implements TickableItem {

    public VomitItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onGroundTick(ItemEntity entity) {
        if (entity.isTouchingWaterOrRain() && entity.age > 10) {
            entity.discard();
        }
    }
}
