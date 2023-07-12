package ivorius.psychedelicraft.client.render.bezier;

import net.minecraft.util.math.Vec3d;

import net.minecraft.util.math.MathHelper;

public interface Bezier {
    static Bezier spiral(double horizontalScale, double verticalScale, double spirals, double maxHeight, double fontSize, double textScale) {
        return spiral(horizontalScale, verticalScale, spirals, new Vec3d(0, maxHeight, 0), fontSize, textScale);
    }

    static Bezier spiral(double horizontalScale, double verticalScale, double spirals, Vec3d terminationPoint, double fontSize, double textScale) {
        return Path.createMemoized(consumer -> {
            final double yx = terminationPoint.y * terminationPoint.x;
            final double yz = terminationPoint.y * terminationPoint.z;
            for (double h0 = 0; h0 <= terminationPoint.getY(); h0 += terminationPoint.getY() / spirals) {
                for (int nodeIndex = 0; nodeIndex < Path.UNIT_VECTORS.length; nodeIndex++) {
                    double h1 = h0 + (terminationPoint.y / spirals) * 0.25 * nodeIndex;
                    if (h1 <= terminationPoint.y) {
                        double d = horizontalScale * (1 + h1 * verticalScale);
                        consumer.accept(
                                Path.UNIT_VECTORS[nodeIndex][0].multiply(d, h1, d).add(yx == 0 ? 0 : h1 / yx, 0, yz == 0 ? 0 : h1 / yz),
                                Path.UNIT_VECTORS[nodeIndex][1].multiply(0.5).multiply(d, 0, d),
                                fontSize + h1 / terminationPoint.y * textScale
                        );
                    }
                }
            }
        });
    }

    static Bezier sphere(double radius, double spirals, double fontSize) {
        return Path.createMemoized(consumer -> {
            final double centerYShift = -radius * 0.5;
            for (double h0 = 0; h0 <= radius; h0 += radius / spirals) {
                for (int nodeIndex = 0; nodeIndex < Path.UNIT_VECTORS.length; nodeIndex++) {
                    double h1 = h0 + (radius / spirals) * 0.25 * nodeIndex;
                    if (h1 <= radius) {
                        double d = MathHelper.cos((float) (h1 / radius - 0.5) * MathHelper.PI);
                        consumer.accept(
                                Path.UNIT_VECTORS[nodeIndex][0].multiply(d, h1, d).add(0, centerYShift, 0),
                                Path.UNIT_VECTORS[nodeIndex][1].multiply(0.5).multiply(d, 0, d),
                                fontSize
                        );
                    }
                }
            }
        });
    }

    Path getPath();
}
