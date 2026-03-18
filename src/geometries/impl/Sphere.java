package geometries.impl;

import primitives.Point;
import primitives.Vector;

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
}