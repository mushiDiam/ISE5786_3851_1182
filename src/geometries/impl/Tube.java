package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an infinite 3D tube (a cylinder without ends).
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis ray of the tube.
     */
    Ray axis;

    /**
     * Constructs a tube with a given radius and central axis.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis ray of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this.axis = axis;
    }

    /**
     * Calculates the normal vector to the tube at a given point.
     *
     * @param point the point on the tube's surface
     * @return the normal vector at the specified point
     */
    @Override
    public Vector getNormal(Point point) {
        return null;
        //TODO
    }
}