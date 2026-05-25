package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a 3D plane defined by a point and a normal vector.
 * 
 * A plane is an infinite flat surface in 3D space. It can be defined either by
 * three non-collinear points or by a point and a normal vector.
 * 
 * @author [Student ID]
 * @version 1.0
 */
public final class Plane extends Geometry {
    /** A point on the plane. */
    private final Point _point;
    
    /** The unit normal vector to the plane. */
    private final Vector _normal;


    /**
     * Constructs a plane from three non-collinear points.
     * 
     * The normal vector is calculated as the cross product of two vectors
     * formed by the three points and is then normalized.
     * 
     * @param p1 the first point on the plane
     * @param p2 the second point on the plane
     * @param p3 the third point on the plane
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        Vector v1 = p2.subtract(p1);
        Vector v2 = p3.subtract(p1);
        _normal = v1.crossProduct(v2).normalize();
    }

    /**
     * Constructs a plane from a point and a normal vector.
     * 
     * The normal vector is normalized to ensure unit length.
     * 
     * @param point  a point on the plane
     * @param normal the normal vector to the plane
     */
    public Plane(Point point, Vector normal) {
        _point = point;
        _normal = normal.normalize();
    }


    /**
     * Returns the normal vector to the plane.
     * 
     * Since the plane is infinite and flat, the normal is the same at every point.
     * 
     * @param point a point (unused, but required by the interface)
     * @return the unit normal vector to the plane
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    /**
     * Calculates intersections between a ray and the plane.
     * 
     * An intersection occurs when the ray intersects the plane at a parameter t > 0.
     * The method respects the maximum distance constraint by filtering out
     * intersections beyond the specified distance.
     * 
     * @param ray         the ray to intersect with the plane
     * @param maxDistance the maximum allowed distance for an intersection
     * @return a list containing a single intersection if found, or null if:
     *         - the ray origin is on the plane
     *         - the ray is parallel to the plane
     *         - the intersection point is behind the ray origin
     *         - the intersection is beyond the maximum distance
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();
        Vector n = _normal;

        if (_point.equals(p0)) {
            return null;
        }

        double nv = n.dotProduct(v);
        if (primitives.Util.isZero(nv)) {
            return null;
        }

        Vector p0ToQ0 = _point.subtract(p0);
        double nQMinusP0 = n.dotProduct(p0ToQ0);
        double t = primitives.Util.alignZero(nQMinusP0 / nv);

        // Check if point is behind the ray OR beyond the maxDistance
        if (t <= 0 || primitives.Util.alignZero(t - maxDistance) > 0) {
            return null;
        }

        return List.of(new Intersection(this, ray.getPoint(t)));
    }
}