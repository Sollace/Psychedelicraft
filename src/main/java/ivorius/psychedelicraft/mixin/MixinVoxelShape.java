package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

@Mixin(VoxelShape.class)
abstract class MixinVoxelShape {
    // Raytrace fix for block collissions that extend beyond their tile
    @Inject(method = "raycast", at = @At("RETURN"), cancellable = true)
    private void raycast(Vec3d start, Vec3d end, BlockPos pos, CallbackInfoReturnable<BlockHitResult> info) {
        BlockHitResult result = info.getReturnValue();
        if (result == null) {
            return;
        }

        Vec3d diff = result.getPos().subtract(Vec3d.ofCenter(result.getBlockPos()));
        final double maxDiff = 1.0000001;
        if (Math.abs(diff.getX()) < maxDiff && Math.abs(diff.getY()) < maxDiff && Math.abs(diff.getZ()) < maxDiff) {
            return;
        }

        info.setReturnValue(result.withBlockPos(new BlockPos(result.getPos())));
    }
}
