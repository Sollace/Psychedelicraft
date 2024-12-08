package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 25.11.14.
 */
public class AgaveFluid extends AlcoholicFluid {
    public AgaveFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.isOf(PSItems.SHOT_GLASS);
    }
    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_SHOT_GLASS_RECEPTICALTS;
    }
}
