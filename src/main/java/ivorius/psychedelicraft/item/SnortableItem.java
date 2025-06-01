package ivorius.psychedelicraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.item.consume.UseAction;
import net.minecraft.world.World;

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

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.random.nextInt(3) == 0) {
            user.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 0.1F, 1.6F);
        }
    }
}
