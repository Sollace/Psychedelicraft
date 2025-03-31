package ivorius.psychedelicraft.item;

import net.minecraft.entity.ItemEntity;

public interface TickableItem {
    void onGroundTick(ItemEntity entity);
}
