package geometries.impl;

import primitives.Point;

/**
 * Represents a 2D triangle in 3D space.
 */
public class Triangle extends Polygon {

    /**
     * Constructs a triangle using 3 points.
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }
}