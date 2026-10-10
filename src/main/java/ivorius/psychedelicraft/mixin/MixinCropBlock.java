package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import ivorius.psychedelicraft.block.PSBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;

@Mixin(value = { CropBlock.class, PlantBlock.class })
abstract class MixinCropBlock {
    @WrapOperation(method = {
            "getAvailableMoisture(Lnet/minecraft/block/Block;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F",
            "canPlantOnTop(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"
        },
        at = @At(value = "INVOKE", target = "net/minecraft/block/BlockState.isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0),
        allow = 2
    )
    private static boolean onIsFarmland(BlockState state, Block block, Operation<Boolean> operation) {
        return operation.call(state, block) || operation.call(state, PSBlocks.PLANTER_FARMLAND);
    }
}
