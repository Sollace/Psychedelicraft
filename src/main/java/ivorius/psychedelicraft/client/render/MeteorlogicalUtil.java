package ivorius.psychedelicraft.client.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public interface MeteorlogicalUtil {

    static float getSunFlareIntensity(@Nullable World world, @Nullable Entity entity, float tickDelta) {

        if (world == null || entity == null) {
            return 0;
        }

        if (world.getDimension().hasCeiling() || !world.getDimension().hasSkyLight()) {
            return 0;
        }

        if (entity.raycast(100, 1, true).getType() != Type.MISS) {
            return 0;
        }

        float sunRadians = world.getSkyAngleRadians(tickDelta);


        Vec3f sunPositionOnScreen = PsycheMatrixHelper.projectPointCurrentView(
                PsycheMatrixHelper.fromPolar(sunRadians, 120)
        );
        sunPositionOnScreen.normalize();

        if (sunPositionOnScreen.getZ() < 0) {
            return 0;
        }

        Window window = MinecraftClient.getInstance().getWindow();
        float maxFlareDistance = 170F;

        sunPositionOnScreen.multiplyComponentwise(window.getScaledWidth(), window.getScaledHeight(), 0);

        return 1 - (Math.min(maxFlareDistance,
                MathHelper.sqrt(
                        (sunPositionOnScreen.getX() * sunPositionOnScreen.getX()) + (sunPositionOnScreen.getY() * sunPositionOnScreen.getY())
                )) / maxFlareDistance);
    }

    static float getSunIntensity(World world) {
        float skyAngle = getSkyAngle(world);
        if (skyAngle > 1) {
            return 0;
        }

        // intensity (0-1) has a peak at 0.5 (midday)
        float intensity = MathHelper.cos((skyAngle - 0.5F) * MathHelper.PI);

        if (world.isRaining()) {
            intensity *= 0.5;
        }
        if (world.isThundering()) {
            intensity *= 0.5;
        }

        return intensity;
    }

    // we translate sun angle to a scale of 0-1 (0=sunrise, 1=sunset, >1 nighttime)
    static float getSkyAngle(World world) {
        return ((world.getSkyAngle(1) + 0.25F) % 1F) * 2;
    }

    static float getSkyLightIntensity(World world, BlockPos pos) {
        if (world.isClient) {
            world.calculateAmbientDarkness();
        }

        return world.getLightLevel(LightType.SKY, pos) / 15F;
    }
}