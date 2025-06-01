package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.item.SmokeableItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class MixinAbstractFurnaceBlockEntity {
    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "net/minecraft/item/ItemStack.decrement(I)V"
        )
    )
    private static void onTick(ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo info) {
        ItemStack fuel = blockEntity.getStack(1);
        if (fuel.getItem() instanceof SmokeableItem smokeable && world instanceof ServerWorld sw) {
            smokeable.onIncinerated(fuel, sw, pos, blockEntity);
        }
    }
}
