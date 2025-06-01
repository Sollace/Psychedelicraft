package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class JollyRancherItem extends EdibleItem {

    public JollyRancherItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            DrugProperties.of(user).ifPresent(properties -> {
                if (properties.cureAll()) {
                    properties.asEntity().sendMessage(Text.translatable(getTranslationKey(stack) + ".use"), true);
                }
            });
        }
        return super.finishUsing(stack, world, user);
    }
}
