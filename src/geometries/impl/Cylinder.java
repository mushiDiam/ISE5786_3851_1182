package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a finite cylinder in 3D space.
 */
public class Cylinder extends Tube {
    /**
     * The height of the cylinder.
     */
    private final double _height;

    /**
     * Constructs a cylinder with a given radius, axis ray, and height.
     *
     * @param radius the radius of the cylinder
     * @param axis   the central axis ray
     * @param height the height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;
    }

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
     * Finds intersections between a ray and the finite cylinder.
     * Evaluates intersections with the side surface (using Tube logic bounded by height)
     * and intersections with the two bases.
     *
     * @param ray the ray to intersect with the cylinder
     * @return a list of intersections, or null if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        // CRITICAL FIX: Call the helper method of the superclass (Tube) per instructions
        List<Intersection> tubeIntersections = super.calcIntersectionsHelper(ray);

        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point pa1 = _axis.origin();
        Vector va = _axis.direction();
        Point pa2 = pa1.add(va.scale(_height));

        double t1 = -1, t2 = -1;
        int count = 0;

        // 1. Check side intersections from Tube (STRICTLY bounded by height)
        if (tubeIntersections != null) {
            for (Intersection gp : tubeIntersections) {
                Point p = gp.point; // Extract the actual point from the Intersection

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
        if (vDotVa != 0) { // If vDotVa == 0, ray is parallel to bases (orthogonal to axis). Bases are ignored.
            boolean isParallelToAxis = primitives.Util.isZero(Math.abs(vDotVa) - 1);

            // Base 1 (bottom)
            double tBase1 = -1;
            if (!p0.equals(pa1)) {
                tBase1 = primitives.Util.alignZero(va.dotProduct(pa1.subtract(p0)) / vDotVa);
            }
            if (tBase1 > 0) {
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
            if (tBase2 > 0) {
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
            return List.of(new Intersection(this, ray.getPoint(t1))); // Fixed type
        } else if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        return null;
    }

    /**
     * Creates a sorted list of cylinder intersections according to their distance
     * from the ray origin.
     *
     * @param ray the ray used to calculate the intersection points
     * @param tA  the first intersection distance parameter
     * @param tB  the second intersection distance parameter
     * @return a list of intersections sorted by distance from the ray origin
     */
    private List<Intersection> createSortedList(Ray ray, double tA, double tB) {
        if (tA < tB) {
            return List.of(new Intersection(this, ray.getPoint(tA)), new Intersection(this, ray.getPoint(tB))); // Fixed types
        }
        return List.of(new Intersection(this, ray.getPoint(tB)), new Intersection(this, ray.getPoint(tA))); // Fixed types
    }
}