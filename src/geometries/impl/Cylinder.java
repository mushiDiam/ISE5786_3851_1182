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
     * @return a list of intersection points, or null if there are none
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> tubeIntersections = super.findIntersections(ray);
        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point pa1 = _axis.origin();
        Vector va = _axis.direction();
        Point pa2 = pa1.add(va.scale(_height));

        double t1 = -1, t2 = -1;
        int count = 0;

        // 1. Check side intersections from Tube bounded by height
        if (tubeIntersections != null) {
            for (Point p : tubeIntersections) {
                double tAxis = primitives.Util.alignZero(va.dotProduct(p.subtract(pa1)));
                if (tAxis > 0 && tAxis < _height) { // Strictly between bases
                    double distance = p0.distance(p);
                    if (count == 0) t1 = distance;
                    else t2 = distance;
                    count++;
                }
            }
        }

        if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        // 2. Check base 1 (bottom)
        double vDotVa = primitives.Util.alignZero(v.dotProduct(va));
        if (vDotVa != 0) { // Not parallel to base
            try {
                double tBase1 = primitives.Util.alignZero(va.dotProduct(pa1.subtract(p0)) / vDotVa);
                if (tBase1 > 0) {
                    Point pBase1 = ray.getPoint(tBase1);
                    if (primitives.Util.alignZero(pBase1.distanceSquared(pa1) - _radiusSquared) < 0) {
                        if (count == 0) t1 = tBase1;
                        else t2 = tBase1;
                        count++;
                    }
                }
            } catch (IllegalArgumentException e) {
                // p0 is on pa1, handled by tBase1 <= 0 logic since intersection must be t > 0
            }
        }

        if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        // 3. Check base 2 (top)
        if (vDotVa != 0) {
            try {
                double tBase2 = primitives.Util.alignZero(va.dotProduct(pa2.subtract(p0)) / vDotVa);
                if (tBase2 > 0) {
                    Point pBase2 = ray.getPoint(tBase2);
                    if (primitives.Util.alignZero(pBase2.distanceSquared(pa2) - _radiusSquared) < 0) {
                        if (count == 0) t1 = tBase2;
                        else t2 = tBase2;
                        count++;
                    }
                }
            } catch (IllegalArgumentException e) {}
        }

        if (count == 1) {
            return List.of(ray.getPoint(t1));
        } else if (count == 2) {
            return createSortedList(ray, t1, t2);
        }

        return null;
    }

    /**
     * Helper method to return a sorted list of points based on t1 and t2.
     */
    private List<Point> createSortedList(Ray ray, double tA, double tB) {
        if (tA < tB) {
            return List.of(ray.getPoint(tA), ray.getPoint(tB));
        }
        return List.of(ray.getPoint(tB), ray.getPoint(tA));
    }
}