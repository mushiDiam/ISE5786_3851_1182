package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents a 3D plane.
 */
public final class Plane extends Geometry {
    /**
     * A point on the plane.
     */
    private final Point _point;

    /**
     * The normal vector to the plane.
     */
    private final Vector _normal;

    /**
     * Constructor for Plane using 3 points.
     * At this stage, it only saves the first point.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        Vector v1 = p2.subtract(p1);
        Vector v2 = p3.subtract(p1);
        _normal = v1.crossProduct(v2).normalize();
    }

    /**
     * Constructor for Plane using a point and a normal vector.
     *
     * @param point  a point on the plane
     * @param normal the normal vector to the plane
     */
    public Plane(Point point, Vector normal) {
        _point = point;
        _normal = normal.normalize();
    }

    /**
     * Retrieves the normal vector to the plane.
     *
     * @param point a point on the plane
     * @return the normal vector of the plane
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    /**
     * Finds intersections between a ray and the plane.
     *
     * @param ray the ray to intersect with the plane
     * @return a list of intersection points, or null if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
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

        return t <= 0 ? null : List.of(new Intersection(this, ray.getPoint(t)));
    }
}