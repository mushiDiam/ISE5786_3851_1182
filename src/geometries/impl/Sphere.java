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

    @Override
    public Vector getNormal(Point point) {
        return null; // TODO
    }
}