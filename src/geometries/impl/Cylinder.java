package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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
        return null; // TODO: normal
    }
}