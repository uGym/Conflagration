package fi.ugim.conflagration.utils;

import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;

public class MathUtil {

    public static final Random RANDOM = new Random();

    public static Vector3d rotateX(Vector3d v, double theta) {

        final double cos = Math.cos(theta);
        final double sin = Math.sin(theta);
        final double y = cos * v.y() - sin * v.z();
        final double z = sin * v.y() + cos * v.z();

        return new Vector3d(v.x(), y, z);
    }

    public static Vector3d rotateY(Vector3d v, double theta) {

        final double cos = Math.cos(theta);
        final double sin = Math.sin(theta);
        final double x = cos * v.x() + sin * v.z();
        final double z = -sin * v.x() + cos * v.z();

        return new Vector3d(x, v.y(), z);
    }

    public static Vector3d rotateZ(Vector3d v, double theta) {

        final double cos = Math.cos(theta);
        final double sin = Math.sin(theta);
        final double x = cos * v.x() - sin * v.y();
        final double y = sin * v.x() + cos * v.y();

        return new Vector3d(x, y, v.z());
    }

    public static double angleBetween(Vector3d v, Vector3d u) {
        return Math.acos(v.dot(u) / (v.length() * u.length()));
    }

    public static Vector3d rotateVector(Vector3d v, Vector3d u, double theta) {

        if (u.lengthSquared() < 1e-10) {
            return v;
        }

        final Vector3d w = u.normalize();
        final double cos = Math.cos(theta);
        final double sin = Math.sin(theta);

        return v.mul(cos).add(w.cross(v).mul(sin)).add(w.mul(w.dot(v) * (1 - cos)));
    }

    public static Vector3d rotateTowards(Vector3d v, Vector3d u, double theta) {
        final Vector3d relative = v.normalize().cross(u.normalize());
        return angleBetween(u, v) > Math.PI ? rotateVector(v, relative, -theta) : rotateVector(v, relative, theta);
    }

    public static Vector3d relativeVertical(Vector3d v) {
        final Vector3d cross = Math.random() > 0.5 ? new Vector3d(0, 0, 1) : new Vector3d(1, 0, 0);
        return v.x() == 0 && v.z() == 0 ? v.cross(cross).cross(v) : v.cross(new Vector3d(0, 1, 0)).cross(v);
    }

    public static Vector3d toEulerAngles(Vector3d v) {

        v = v.normalize();

        return new Vector3d(
            (Math.toDegrees(Math.asin(v.y())) + 90) % 360,
            (Math.toDegrees(Math.atan2(-v.x(), v.z())) + 180) % 360,
            0
        );
    }

    public static Set<Vector3i> horizontalDisk(Vector3i center, int radius) {

        final Set<Vector3i> positions = new HashSet<>();
        final int x = center.x();
        final int y = center.y();
        final int z = center.z();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    positions.add(new Vector3i(x + i, y, z + j));
                }
            }
        }

        return positions;
    }

    public static Set<Vector3i> sphere(Vector3i center, int radius) {

        final Set<Vector3i> positions = new HashSet<>();
        final int x = center.x();
        final int y = center.y();
        final int z = center.z();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    if (i * i + j * j + k * k <= radius * radius) {
                        positions.add(new Vector3i(x + i, y + j, z + k));
                    }
                }
            }
        }

        return positions;
    }

    public static List<Set<Vector3i>> layeredSphere(Vector3i center, int maxRadius) {

        final List<Set<Vector3i>> layeredSpheres = new ArrayList<>();

        for (int r = 0; r <= maxRadius; r++) {

            final Set<Vector3i> shell = new HashSet<>();
            final int x = center.x();
            final int y = center.y();
            final int z = center.z();

            for (int i = -r; i <= r; i++) {
                for (int j = -r; j <= r; j++) {
                    for (int k = -r; k <= r; k++) {
                        final int radius = i * i + j * j + k * k;
                        if (radius <= r * r && radius >= (r - 1) * (r - 1)) {
                            shell.add(new Vector3i(x + i, y + j, z + k));
                        }
                    }
                }
            }

            layeredSpheres.add(shell);
        }

        return layeredSpheres;
    }

}
