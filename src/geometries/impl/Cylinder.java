package geometries.impl;

import geometries.api.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a finite cylinder in 3D space.
 * <p>
 * A cylinder is defined by a central axis (a ray), a radius, and a height.
 * Unlike a tube (infinite cylinder), a finite cylinder has two circular bases
 * at the ends. The surface consists of the lateral (side) surface and two bases.
 *
 * @author [Student ID]
 * @version 1.0
 */
public class Cylinder extends Tube {
    /**
     * The height of the cylinder measured along the axis direction.
     */
    private final double _height;


    /**
     * Constructs a finite cylinder with a given radius, axis ray, and height.
     *
     * @param radius the radius of the cylinder (must be positive)
     * @param axis   the central axis ray (origin is at the bottom base)
     * @param height the height of the cylinder along the axis (must be positive)
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;
    }


    /**
     * Returns the normal vector to the cylinder at a given point on its surface.
     * <p>
     * The normal direction depends on where the point is located:
     * - On the bottom base: points downward (opposite to axis direction)
     * - On the top base: points upward (same as axis direction)
     * - On the side surface: points radially outward from the axis
     *
     * @param point a point on the cylinder surface
     * @return a unit normal vector perpendicular to the surface at the point
     */
    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        // BV02: Point is exactly at the center of the bottom base
        if (point.equals(p0)) {
            return v.scale(-1);
        }

        // Calculate the projection scalar 't' of the point on the axis
        double t = v.dotProduct(point.subtract(p0));

        // EP03 & BV04: Point is on the bottom base (t == 0)
        if (primitives.Util.isZero(t)) {
            return v.scale(-1);
        }

        // EP02 & BV01 & BV03: Point is on the top base (t == height)
        if (primitives.Util.isZero(t - _height)) {
            return v;
        }

        // EP01: Point is on the side surface, so we can use the Tube's logic
        return super.getNormal(point);
    }

    /**
     * Calculates intersections between a ray and the finite cylinder.
     * <p>
     * The method evaluates intersections in three stages:
     * 1. Side surface: Uses Tube logic but strictly bounded by the height limits
     * 2. Bottom base: Intersection with the circular base at the axis origin
     * 3. Top base: Intersection with the circular base at the axis origin + height*direction
     * <p>
     * Intersections are filtered to respect the maximum distance constraint.
     * The method returns at most 2 intersection points (the ray can enter and exit the cylinder,
     * or intersect one or both bases).
     *
     * @param ray         the ray to intersect with the cylinder
     * @param maxDistance the maximum distance to search for intersections
     * @return a sorted list of intersections (0, 1, or 2 points), or null if no
     * intersections are found within the specified distance
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // CRITICAL FIX: Pass the maxDistance to the superclass (Tube)
        List<Intersection> tubeIntersections = super.calcIntersectionsHelper(ray, maxDistance);

        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point pa1 = _axis.origin();
        Vector va = _axis.direction();
        Point pa2 = pa1.add(va.scale(_height));

        double t1 = -1, t2 = -1;
        int count = 0;

        // 1. Check side intersections from Tube (STRICTLY bounded by height)
        // Tube already filtered out intersections beyond maxDistance.
        if (tubeIntersections != null) {
            for (Intersection gp : tubeIntersections) {
                Point p = gp.point;

                double tAxis = 0;
                if (!p.equals(pa1)) {
                    tAxis = primitives.Util.alignZero(va.dotProduct(p.subtract(pa1)));
                }

                // Strictly inside the cylinder. Rims are handled by the bases.
                if (tAxis > 0 && primitives.Util.alignZero(tAxis - _height) < 0) {
                    double distance = p0.distance(p);
                    if (count == 0) t1 = distance;
                    else t2 = distance;
                    count++;
                }
            }
        }

        // If we found 2 points on the side, we don't need to check the bases
        if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        // 2. Check bases
        double vDotVa = primitives.Util.alignZero(v.dotProduct(va));
        if (vDotVa != 0) { // If vDotVa == 0, ray is parallel to bases. Bases are ignored.
            boolean isParallelToAxis = primitives.Util.isZero(Math.abs(vDotVa) - 1);

            // Base 1 (bottom)
            double tBase1 = -1;
            if (!p0.equals(pa1)) {
                tBase1 = primitives.Util.alignZero(va.dotProduct(pa1.subtract(p0)) / vDotVa);
            }

            // BONUS FILTER: Only consider if tBase1 > 0 AND tBase1 <= maxDistance
            if (tBase1 > 0 && primitives.Util.alignZero(tBase1 - maxDistance) <= 0) {
                Point pBase1 = ray.getPoint(tBase1);
                double dSquared = pBase1.distanceSquared(pa1);
                double check = primitives.Util.alignZero(dSquared - _radiusSquared);

                // If parallel to axis, reject the rim (check < 0). Otherwise, accept the rim (check <= 0).
                if ((isParallelToAxis && check < 0) || (!isParallelToAxis && check <= 0)) {
                    if (count == 0) t1 = tBase1;
                    else t2 = tBase1;
                    count++;
                }
            }

            if (count == 2) {
                return createSortedList(ray, t1, t2);
            }

            // Base 2 (top)
            double tBase2 = -1;
            if (!p0.equals(pa2)) {
                tBase2 = primitives.Util.alignZero(va.dotProduct(pa2.subtract(p0)) / vDotVa);
            }

            // BONUS FILTER: Only consider if tBase2 > 0 AND tBase2 <= maxDistance
            if (tBase2 > 0 && primitives.Util.alignZero(tBase2 - maxDistance) <= 0) {
                Point pBase2 = ray.getPoint(tBase2);
                double dSquared = pBase2.distanceSquared(pa2);
                double check = primitives.Util.alignZero(dSquared - _radiusSquared);

                if ((isParallelToAxis && check < 0) || (!isParallelToAxis && check <= 0)) {
                    if (count == 0) t1 = tBase2;
                    else t2 = tBase2;
                    count++;
                }
            }
        }

        // 3. Final return
        if (count == 1) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        } else if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        return null;
    }


    /**
     * Creates a sorted list of intersections based on distance from ray origin.
     * <p>
     * Ensures that the intersection points are ordered by their distance from the ray origin,
     * with the closer intersection point appearing first in the list.
     *
     * @param ray the ray for reference to calculate point positions
     * @param tA  the distance parameter for the first intersection point
     * @param tB  the distance parameter for the second intersection point
     * @return a list of two Intersection objects sorted by distance
     */
    private List<Intersection> createSortedList(Ray ray, double tA, double tB) {
        if (tA < tB) {
            return List.of(new Intersection(this, ray.getPoint(tA)), new Intersection(this, ray.getPoint(tB)));
        }
        return List.of(new Intersection(this, ray.getPoint(tB)), new Intersection(this, ray.getPoint(tA)));
    }

    @Override
    protected AABB calculateBoundingBox() {
        Point base = _axis.origin();
        Point top = _axis.getPoint(_height);
        return new AABB(
                Math.min(base.getX(), top.getX()) - _radius,
                Math.min(base.getY(), top.getY()) - _radius,
                Math.min(base.getZ(), top.getZ()) - _radius,
                Math.max(base.getX(), top.getX()) + _radius,
                Math.max(base.getY(), top.getY()) + _radius,
                Math.max(base.getZ(), top.getZ()) + _radius);
    }
}