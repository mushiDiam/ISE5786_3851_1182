package geometries.impl;

import geometries.api.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents an infinite tube (infinite cylinder) in 3D space.
 * <p>
 * A tube is defined by a central axis (a ray) and a radius. It extends infinitely
 * along both directions of the axis. The surface of the tube consists of all points
 * at a fixed distance (radius) from the axis.
 *
 * @author [Student ID]
 * @version 1.0
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis of the tube as a ray.
     */
    protected final Ray _axis;


    /**
     * Constructs a tube with a given radius and axis.
     *
     * @param radius the radius of the tube (must be positive)
     * @param axis   the central axis of the tube as a ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }


    /**
     * Returns the normal vector to the tube at a given point on its surface.
     * <p>
     * The normal is perpendicular to the axis and points from the axis
     * to the given point on the surface.
     *
     * @param point a point on the tube surface
     * @return a unit normal vector
     */
    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();
        Vector p0ToPoint = point.subtract(p0);
        double t = v.dotProduct(p0ToPoint);
        Point o = p0;

        if (!primitives.Util.isZero(t)) {
            o = p0.add(v.scale(t));
        }
        return point.subtract(o).normalize();
    }

    /**
     * Calculates intersections between a ray and the tube.
     * <p>
     * Finds where the ray intersects the infinite cylindrical surface, using a
     * quadratic equation derived from the distance formula. The method filters
     * out intersections beyond the specified maximum distance.
     *
     * @param ray         the ray to intersect with the tube
     * @param maxDistance the maximum allowed distance for an intersection
     * @return a list of intersections (0, 1, or 2 points), or null if no
     * intersections exist within the specified distance
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point pa = _axis.origin();
        Vector va = _axis.direction();

        Vector deltaP = null;
        try {
            deltaP = p0.subtract(pa);
        } catch (IllegalArgumentException e) {
            // p0 == pa
        }

        double vDotVa = primitives.Util.alignZero(v.dotProduct(va));
        Vector vMinusVaVdotVa = v;

        if (vDotVa != 0) {
            try {
                vMinusVaVdotVa = v.subtract(va.scale(vDotVa));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        double a = primitives.Util.alignZero(vMinusVaVdotVa.lengthSquared());
        double b = 0;
        double c = -_radiusSquared;

        if (deltaP != null) {
            double dpDotVa = primitives.Util.alignZero(deltaP.dotProduct(va));
            Vector dpMinusVaDpDotVa = deltaP;

            if (dpDotVa != 0) {
                try {
                    dpMinusVaDpDotVa = deltaP.subtract(va.scale(dpDotVa));
                } catch (IllegalArgumentException e) {
                    dpMinusVaDpDotVa = null;
                }
            }

            if (dpMinusVaDpDotVa != null) {
                b = primitives.Util.alignZero(2 * vMinusVaVdotVa.dotProduct(dpMinusVaDpDotVa));
                c += primitives.Util.alignZero(dpMinusVaDpDotVa.lengthSquared());
            }
        }

        double discriminant = primitives.Util.alignZero(b * b - 4 * a * c);

        if (discriminant <= 0) {
            return null;
        }

        double sqrtDiscr = Math.sqrt(discriminant);
        double t1 = primitives.Util.alignZero((-b - sqrtDiscr) / (2 * a));
        double t2 = primitives.Util.alignZero((-b + sqrtDiscr) / (2 * a));

        boolean t1Valid = t1 > 0 && primitives.Util.alignZero(t1 - maxDistance) <= 0;
        boolean t2Valid = t2 > 0 && primitives.Util.alignZero(t2 - maxDistance) <= 0;

        if (t1Valid && t2Valid) {
            return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        }
        if (t1Valid) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        }
        if (t2Valid) {
            return List.of(new Intersection(this, ray.getPoint(t2)));
        }

        return null;
    }

    @Override
    protected AABB calculateBoundingBox() {
        return AABB.INFINITE;
    }
}