package geometries.impl;

import geometries.api.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a 3D sphere in a Cartesian coordinate system.
 * <p>
 * A sphere is defined by its center point and a radius. All points on the surface
 * of the sphere are at a fixed distance (the radius) from the center.
 *
 * @author [Student ID]
 * @version 1.0
 */
public class Sphere extends RadialGeometry {
    /**
     * The center point of the sphere.
     */
    private final Point _center;


    /**
     * Constructs a sphere with a given center point and radius.
     *
     * @param center the center of the sphere
     * @param radius the radius of the sphere (must be positive)
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    /**
     * Returns the normal vector to the sphere at a given point on its surface.
     * <p>
     * The normal vector points radially outward from the center through the point.
     *
     * @param point a point on the sphere surface
     * @return a unit normal vector pointing outward from the center
     */
    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    /**
     * Calculates intersections between a ray and the sphere.
     * <p>
     * Uses the standard ray-sphere intersection algorithm. The method filters
     * out intersections that are behind the ray origin or beyond the maximum distance.
     *
     * @param ray         the ray to intersect with the sphere
     * @param maxDistance the maximum allowed distance for an intersection
     * @return a list of intersections (0, 1, or 2 points), or null if no
     * intersections exist within the specified distance
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        if (p0.equals(_center)) {
            if (primitives.Util.alignZero(_radius - maxDistance) <= 0) {
                return List.of(new Intersection(this, ray.getPoint(_radius)));
            }
            return null;
        }

        Vector u = _center.subtract(p0);
        double tm = primitives.Util.alignZero(v.dotProduct(u));
        double dSquared = primitives.Util.alignZero(u.lengthSquared() - tm * tm);
        double thSquared = primitives.Util.alignZero(_radiusSquared - dSquared);

        if (thSquared <= 0) {
            return null;
        }

        double th = primitives.Util.alignZero(Math.sqrt(thSquared));
        double t1 = primitives.Util.alignZero(tm - th);
        double t2 = primitives.Util.alignZero(tm + th);

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
        return new AABB(
                _center.getX() - _radius, _center.getY() - _radius, _center.getZ() - _radius,
                _center.getX() + _radius, _center.getY() + _radius, _center.getZ() + _radius);
    }
}