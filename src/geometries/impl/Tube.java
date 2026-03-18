package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an infinite tube in 3D space.
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis ray of the tube.
     */
    protected final Ray _axis;

    /**
     * Constructs a tube with a given radius and axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        // Vector from the start of the ray to the given point
        Vector p0ToPoint = point.subtract(p0);

        // The scalar projection of p0ToPoint on the direction vector
        double t = v.dotProduct(p0ToPoint);

        // Calculate the closest point on the axis to the given point
        Point o = p0;

        // We use isZero to avoid scaling by exactly 0, which would result in adding a null vector
        if (!primitives.Util.isZero(t)) {
            o = p0.add(v.scale(t));
        }

        // The normal is the normalized vector from the axis point 'o' to the given point
        return point.subtract(o).normalize();
    }
}