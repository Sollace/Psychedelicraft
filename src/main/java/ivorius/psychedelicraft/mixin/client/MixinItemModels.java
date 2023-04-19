package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.item.SuspiciousItem;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(ItemModels.class)
abstract class MixinItemModels {
    @ModifyVariable(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"),
            index = 1)
    private ItemStack modifyStack(ItemStack stack) {
        if (stack.getItem() instanceof SuspiciousItem sus) {
            return sus.getHallucinatedItem().map(Item::getDefaultStack).orElse(stack);
        }
        return stack;
    }
}
