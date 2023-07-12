package ivorius.psychedelicraft.client.render.bezier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.util.math.Vec3d;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;

final class Path {
    static Bezier createMemoized(Consumer<NodeConsumer> nodeFactory) {
        return Suppliers.memoize(() -> new Path(nodeFactory))::get;
    }

    static final int SAMPLES = 50;
    static final Vec3d[][] UNIT_VECTORS = {
            { new Vec3d(1, 1, -1), new Vec3d(-1, 1, -1) },
            { new Vec3d(-1, 1, -1), new Vec3d(-1, 1, 1) },
            { new Vec3d(-1, 1, 1), new Vec3d(1, 1, 1) },
            { new Vec3d(1, 1, 1), new Vec3d(1, 1, -1) }
    };

    private final List<Node> nodes = new ArrayList<>();
    private final DoubleList distances = new DoubleArrayList();
    private final double[] progresses;
    private double totalDistance;

    /**
     * Represents the last segment along the path.
     */
    private final Supplier<Intermediate> terminal;

    private Path(Consumer<NodeConsumer> nodeFactory) {
        nodeFactory.accept((position, orientation, fontSize) -> {
            if (!nodes.isEmpty()) {
                Node previous = nodes.get(nodes.size() - 1);
                double distance = 0;

                for (int i = 0; i < SAMPLES; i++) {
                    Vec3d from = depart(previous.position(), previous.orientation());
                    Vec3d to = approach(position, orientation);

                    Vec3d point1 = MathUtils.cubicMix(previous.position(), from, to, position, (double) i / (double) SAMPLES);
                    Vec3d point2 = MathUtils.cubicMix(previous.position(), from, to, position, (double) (i + 1) / (double) SAMPLES);

                    distance += point1.distanceTo(point2);
                }

                totalDistance += distance;
                distances.add(distance);
            }

            nodes.add(new Node(position, orientation, fontSize));
        });

        progresses = distances.doubleStream().map(d -> d / totalDistance).toArray();
        terminal = Suppliers.memoize(() -> {
            int nodeCount = nodes.size();
            Int2DoubleFunction deltaSupplier = index -> IntStream.range(0, index).mapToDouble(i -> progresses[i % progresses.length]).sum();
            return Intermediate.create(
                    nodes.get(nodeCount - 2),
                    nodes.get(nodeCount - 1),
                    deltaSupplier.applyAsDouble(nodeCount - 2),
                    deltaSupplier.applyAsDouble(nodeCount - 1), 1);
        });
    }

    public Intermediate getStep(double delta) {
        delta = ((delta % 1) + 1) % 1;
        double curProgress = 0;

        for (int i = 1; i < nodes.size(); i++) {
            double progress = progresses[i - 1];

            if ((delta - progress) <= 0) {
                return Intermediate.create(nodes.get(i - 1), nodes.get(i), curProgress, curProgress + progress, delta / progress);
            }

            delta -= progress;
            curProgress += progress;
        }

        return terminal.get();
    }

    public Vec3d getNaturalRotation(Intermediate intermediate, double stepSize) {
        return toNatural(toSpherical(getStep(intermediate.delta() + stepSize * 0.3).position().subtract(intermediate.position())));
    }

    private static Vec3d toNatural(Vec3d spherical) {
        return new Vec3d(
            spherical.getX() / (Math.PI * 180),
            spherical.getY() / (Math.PI * 180),
            spherical.getY() + 90
        );
    }

    private static Vec3d toSpherical(Vec3d vector) {
        double r = vector.length();
        double inclination = Math.acos(vector.x / r);
        double azimuth = Math.atan2(vector.y, vector.z);
        return new Vec3d(azimuth, inclination, r);
    }

    interface NodeConsumer {
        void accept(Vec3d position, Vec3d orientation, double fontSize);
    }

    private static Vec3d approach(Vec3d position, Vec3d orientation) {
        return position.subtract(orientation);
    }

    private static Vec3d depart(Vec3d position, Vec3d orientation) {
        return position.add(orientation);
    }

    record Node(Vec3d position, Vec3d orientation, double fontSize) { }

    record Intermediate(Vec3d position, float fontSize, double delta) {
        static Intermediate create(Node prev, Node next, double minDelta, double maxDelta, double betweenDelta) {
            float delta = (float)MathHelper.lerp(betweenDelta, minDelta, maxDelta);
            return new Intermediate(
                    MathUtils.cubicMix(
                            prev.position(),
                            depart(prev.position(), prev.orientation()),
                            approach(next.position(), next.orientation()),
                            next.position(),
                            betweenDelta
                    ),
                    (float)MathHelper.lerp(delta, prev.fontSize(), next.fontSize()),
                    delta
            );
        }

    }
}