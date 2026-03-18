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
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        // BV02: Point is exactly at the center of the bottom base
        if (point.equals(p0)) {
            return v.scale(-1);
        }

        // Calculate the projection scalar 't' of the point on the axis
        double t = v.dotProduct(point.subtract(p0));

        // EP03 & BV04: Point is on the bottom base (t == 0)
        if (primitives.Util.isZero(t)) {
            return v.scale(-1);
        }

        // EP02 & BV01 & BV03: Point is on the top base (t == height)
        if (primitives.Util.isZero(t - _height)) {
            return v;
        }

        // EP01: Point is on the side surface, so we can use the Tube's logic
        return super.getNormal(point);
    }
}