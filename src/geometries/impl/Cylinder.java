package geometries.impl;

import primitives.Ray;

/**
 * Represents a finite 3D cylinder.
 */
public class Cylinder extends Tube {
    /**
     * The height of the cylinder.
     */
    double height;

    /**
     * Constructs a cylinder with a given radius, central axis, and height.
     *
     * @param radius the radius of the cylinder
     * @param axis   the central axis ray of the cylinder
     * @param height the height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this.height = height;
    }
}