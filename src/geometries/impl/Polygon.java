package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.isZero;

/**
 * Represents a convex polygon in a 3D Cartesian coordinate system.
 * 
 * A polygon is defined by a set of vertices that form a planar, convex shape.
 * All vertices must be coplanar and ordered consistently (either all clockwise
 * or all counter-clockwise) to maintain convexity.
 * 
 * @author [Student ID]
 * @version 1.0
 */
public class Polygon extends Geometry {
    /** The list of vertices that define the polygon. */
    protected final List<Point> _vertices;
    
    /** The plane in which the polygon lies. */
    protected final Plane _plane;
    
    /** The number of vertices in the polygon. */
    private final int _size;


    /**
     * Constructs a polygon from a variable number of vertices.
     * 
     * The constructor validates that:
     * - At least 3 vertices are provided (minimum for a polygon)
     * - All vertices are coplanar
     * - The polygon is convex (vertices are ordered consistently)
     * 
     * @param vertices at least 3 points defining the polygon vertices
     * @throws IllegalArgumentException if fewer than 3 vertices are provided,
     *                                  if vertices are not coplanar, or if
     *                                  the polygon is not convex
     */
    public Polygon(Point... vertices) {
        if (vertices.length < 3)
            throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
        _vertices = List.of(vertices);
        _size = vertices.length;

        _plane = new Plane(vertices[0], vertices[1], vertices[2]);
        if (_size == 3) return;

        Vector n = _plane.getNormal(vertices[0]);
        Vector edge1 = vertices[_size - 1].subtract(vertices[_size - 2]);
        Vector edge2 = vertices[0].subtract(vertices[_size - 1]);

        boolean positive = edge1.crossProduct(edge2).dotProduct(n) > 0;
        for (var i = 1; i < _size; ++i) {
            if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
                throw new IllegalArgumentException("All vertices of a polygon must lay in the same plane");
            edge1 = edge2;
            edge2 = vertices[i].subtract(vertices[i - 1]);
            if (positive != (edge1.crossProduct(edge2).dotProduct(n) > 0))
                throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
        }
    }


    /**
     * Returns the normal vector to the polygon at a given point.
     * 
     * The normal is calculated from the plane containing the polygon.
     * 
     * @param point a point on the polygon surface
     * @return a unit normal vector to the polygon
     */
    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    /**
     * Calculates intersections between a ray and the polygon.
     * 
     * Uses the plane intersection first, then checks if the intersection point
     * lies within the polygon using the same-side method (cross product technique).
     * The method filters out intersections beyond the specified maximum distance.
     * 
     * @param ray         the ray to intersect with the polygon
     * @param maxDistance the maximum allowed distance for an intersection
     * @return a list containing a single intersection if found, or null if no
     *         intersection exists within the specified distance
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // Pass maxDistance down to the plane. The plane will handle the distance filtering.
        var planeIntersections = _plane.calcIntersections(ray, maxDistance);
        if (planeIntersections == null) {
            return null;
        }

        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector v1 = _vertices.get(0).subtract(p0);
        Vector v2 = _vertices.get(1).subtract(p0);
        double sign = primitives.Util.alignZero(v.dotProduct(v1.crossProduct(v2)));

        if (sign == 0) {
            return null;
        }

        boolean positive = sign > 0;
        for (int i = 1; i < _vertices.size(); i++) {
            v1 = v2;
            v2 = _vertices.get((i + 1) % _vertices.size()).subtract(p0);
            double currentSign = primitives.Util.alignZero(v.dotProduct(v1.crossProduct(v2)));

            if (currentSign == 0 || (currentSign > 0) != positive) {
                return null;
            }
        }

        // Return the intersection wrapped with the Polygon (this) instead of the Plane
        return List.of(new Intersection(this, planeIntersections.get(0).point));
    }
}