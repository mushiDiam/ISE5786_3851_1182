package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * An <b>Axis-Aligned Bounding Box</b> (AABB): the smallest box, with faces
 * parallel to the X/Y/Z planes, that fully encloses a geometry or a group of
 * geometries.
 *
 * <p>This is the core primitive of the BVH (Bounding Volume Hierarchy)
 * acceleration. A ray-box test is far cheaper than a ray-geometry test, so if a
 * ray misses the box it cannot possibly hit anything inside it, and the
 * expensive geometry test is skipped (this is the CBR — Conservative Bounding
 * Region — optimization).</p>
 *
 * <p>The box is immutable. Unbounded geometries (such as an infinite plane or
 * tube) use {@link #INFINITE}, a box that every ray intersects, so they are
 * never wrongly pruned.</p>
 */
public class AABB {

    /**
     * A box spanning all of space. Used for unbounded geometries (plane, tube):
     * every ray "hits" it, so such geometries are never pruned.
     */
    public static final AABB INFINITE = new AABB(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * Minimum X coordinate of the box.
     */
    private final double _minX;
    /**
     * Minimum Y coordinate of the box.
     */
    private final double _minY;
    /**
     * Minimum Z coordinate of the box.
     */
    private final double _minZ;
    /**
     * Maximum X coordinate of the box.
     */
    private final double _maxX;
    /**
     * Maximum Y coordinate of the box.
     */
    private final double _maxY;
    /**
     * Maximum Z coordinate of the box.
     */
    private final double _maxZ;

    /**
     * Constructs a bounding box from its minimum and maximum coordinates.
     *
     * @param minX minimum X
     * @param minY minimum Y
     * @param minZ minimum Z
     * @param maxX maximum X
     * @param maxY maximum Y
     * @param maxZ maximum Z
     */
    public AABB(double minX, double minY, double minZ,
                double maxX, double maxY, double maxZ) {
        _minX = minX;
        _minY = minY;
        _minZ = minZ;
        _maxX = maxX;
        _maxY = maxY;
        _maxZ = maxZ;
    }

    /**
     * Tests whether a ray intersects this box, using the classic "slab" method:
     * for each axis the box is a slab between two parallel planes; the ray hits
     * the box only if its entry/exit intervals on all three axes overlap.
     *
     * @param ray the ray to test
     * @return {@code true} if the ray meets the box (or starts inside it)
     */
    public boolean intersects(Ray ray) {
        Point origin = ray.origin();
        Vector dir = ray.direction();

        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        // X slab
        double o = origin.getX(), d = dir.getX();
        if (d != 0) {
            double t1 = (_minX - o) / d;
            double t2 = (_maxX - o) / d;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMin > tMax) return false;
        } else if (o < _minX || o > _maxX) {
            return false;
        }

        // Y slab
        o = origin.getY();
        d = dir.getY();
        if (d != 0) {
            double t1 = (_minY - o) / d;
            double t2 = (_maxY - o) / d;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMin > tMax) return false;
        } else if (o < _minY || o > _maxY) {
            return false;
        }

        // Z slab
        o = origin.getZ();
        d = dir.getZ();
        if (d != 0) {
            double t1 = (_minZ - o) / d;
            double t2 = (_maxZ - o) / d;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMin > tMax) return false;
        } else if (o < _minZ || o > _maxZ) {
            return false;
        }

        // The overlap interval is [tMin, tMax]; reject if the whole box is behind the ray
        return tMax >= 0;
    }

    /**
     * Returns the smallest box that contains both this box and another — used
     * when building a hierarchy node from its children.
     *
     * @param other the other box
     * @return the union (combined) box
     */
    public AABB union(AABB other) {
        return new AABB(
                Math.min(_minX, other._minX),
                Math.min(_minY, other._minY),
                Math.min(_minZ, other._minZ),
                Math.max(_maxX, other._maxX),
                Math.max(_maxY, other._maxY),
                Math.max(_maxZ, other._maxZ));
    }

    /**
     * Reports whether this is a real, finite box (as opposed to {@link #INFINITE}).
     * Unbounded geometries (plane, tube) produce an infinite box and must be kept
     * out of the spatial tree, since they have no meaningful center to sort by.
     *
     * @return {@code true} if all six bounds are finite
     */
    public boolean isFinite() {
        return Double.isFinite(_minX) && Double.isFinite(_minY) && Double.isFinite(_minZ)
                && Double.isFinite(_maxX) && Double.isFinite(_maxY) && Double.isFinite(_maxZ);
    }

    /**
     * Calculates the X coordinate of the center of the bounding box.
     *
     * @return the X coordinate of the center
     */
    public double getCenterX() {
        return (_minX + _maxX) / 2;
    }

    /**
     * Calculates the Y coordinate of the center of the bounding box.
     *
     * @return the Y coordinate of the center
     */
    public double getCenterY() {
        return (_minY + _maxY) / 2;
    }

    /**
     * Calculates the Z coordinate of the center of the bounding box.
     *
     * @return the Z coordinate of the center
     */
    public double getCenterZ() {
        return (_minZ + _maxZ) / 2;
    }
}