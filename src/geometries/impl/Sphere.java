package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a 3D sphere.
 */
public class Sphere extends RadialGeometry {
    /**
     * The center point of the sphere.
     */
    private final Point _center;

    /**
     * Constructs a sphere with a given center point and radius.
     *
     * @param center the center point of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    /**
     * Calculates the normal vector to the sphere at a given point.
     *
     * @param point the point on the sphere's surface
     * @return the normal vector at the specified point
     */
    @Override
    public Vector getNormal(Point point) {
        // The normal to a sphere at a given point is the normalized vector
        // starting from the center and ending at the point.
        return point.subtract(_center).normalize();
    }

    /**
     * Finds intersections between a ray and the sphere.
     *
     * @param ray the ray to intersect with the sphere
     * @return a list of intersection points, or null if there are none
     */
    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        if (p0.equals(_center)) {
            return List.of(new GeoPoint(this, ray.getPoint(_radius)));
        }

        Vector u = _center.subtract(p0);
        double tm = primitives.Util.alignZero(v.dotProduct(u));
        double dSquared = primitives.Util.alignZero(u.lengthSquared() - tm * tm);
        double thSquared = primitives.Util.alignZero(_radiusSquared - dSquared);

        if (thSquared <= 0) {
            return null; // No intersection or tangent
        }

        double th = primitives.Util.alignZero(Math.sqrt(thSquared));
        double t1 = primitives.Util.alignZero(tm - th);
        double t2 = primitives.Util.alignZero(tm + th);

        if (t1 > 0 && t2 > 0) {
            return List.of(new GeoPoint(this, ray.getPoint(t1)), new GeoPoint(this, ray.getPoint(t2)));
        }
        if (t1 > 0) {
            return List.of(new GeoPoint(this, ray.getPoint(t1)));
        }
        if (t2 > 0) {
            return List.of(new GeoPoint(this, ray.getPoint(t2)));
        }

        return null;
    }
}