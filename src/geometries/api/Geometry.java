package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract base class for all geometric shapes.
 */
public abstract class Geometry {

    /**
     * Computes the normal vector to the geometry at a given point.
     *
     * @param point the point on the geometry surface
     * @return the normal vector at the given point
     */
    public abstract Vector getNormal(Point point);
}