package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

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

    /**
     * Finds intersections between a ray and the triangle.
     * Uses the normal vectors of the edges to determine if the intersection point
     * on the plane is inside the triangle.
     *
     * @param ray the ray to intersect with the triangle
     * @return a list containing the intersection point, or null if there is none
     */
    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        // First, check if the ray intersects the plane of the triangle
        var planeIntersections = _plane.findGeoIntersections(ray);
        if (planeIntersections == null) {
            return null;
        }

        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector v1 = _vertices.get(0).subtract(p0);
        Vector v2 = _vertices.get(1).subtract(p0);
        Vector v3 = _vertices.get(2).subtract(p0);

        Vector n1 = v1.crossProduct(v2).normalize();
        Vector n2 = v2.crossProduct(v3).normalize();
        Vector n3 = v3.crossProduct(v1).normalize();

        double s1 = primitives.Util.alignZero(v.dotProduct(n1));
        double s2 = primitives.Util.alignZero(v.dotProduct(n2));
        double s3 = primitives.Util.alignZero(v.dotProduct(n3));

        if (s1 == 0 || s2 == 0 || s3 == 0) {
            return null;
        }

        if ((s1 > 0 && s2 > 0 && s3 > 0) || (s1 < 0 && s2 < 0 && s3 < 0)) {
            // The point is inside the triangle, return it wrapped with 'this' (the Triangle)
            return List.of(new GeoPoint(this, planeIntersections.get(0).point));
        }

        return null;
    }
}