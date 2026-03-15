package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

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
        _normal = null;
        // TODO:
        // Vector v1 = p2.subtract(p1);
        // Vector v2 = p3.subtract(p2);
        // _normal = v1.crossProduct(v2).normalize();
        // for later use
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

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }
}