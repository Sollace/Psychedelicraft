package ivorius.psychedelicraft.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class PacifierItem extends Item implements Equipment {
    public PacifierItem(Settings settings) {
        super(settings);

    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.OFFHAND;
    }

    public static boolean consumePacifier(LivingEntity entity) {
        return consumePacifier(entity, Hand.MAIN_HAND) || consumePacifier(entity, Hand.OFF_HAND);
    }

    private static boolean consumePacifier(LivingEntity entity, Hand hand) {
        ItemStack pacifier = entity.getStackInHand(hand);
        if (pacifier.isOf(PSItems.PACIFIER)) {
            pacifier.damage(1, entity, e -> e.sendToolBreakStatus(hand));
            return true;
        }
        return false;
    }
}
