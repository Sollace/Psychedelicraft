package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

@Mixin(VoxelShape.class)
abstract class MixinVoxelShape {
    // Raytrace fix for block collisions that extend beyond their tile
    @ModifyReturnValue(method = "raycast", at = @At("RETURN"))
    private BlockHitResult raycast(BlockHitResult result, Vec3d start, Vec3d end, BlockPos pos) {
        if (result == null) {
            return result;
        }

        Vec3d diff = result.getPos().subtract(Vec3d.ofCenter(result.getBlockPos()));
        final double maxDiff = 1.0000001;
        if (Math.abs(diff.getX()) < maxDiff && Math.abs(diff.getY()) < maxDiff && Math.abs(diff.getZ()) < maxDiff) {
            return result;
        }

        return result.withBlockPos(BlockPos.ofFloored(result.getPos()));
    }
}
