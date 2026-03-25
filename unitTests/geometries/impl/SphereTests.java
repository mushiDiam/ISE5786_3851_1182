package geometries.impl;

import geometries.impl.Sphere;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.impl.Sphere} class.
 */
class SphereTests {
    private static final Point p100 = new Point(1, 0, 0);
    private static final Sphere SPHERE = new Sphere(p100, 1d);
    /**
     * Test method for {@link geometries.impl.Sphere#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Sphere with radius 5, centered at the origin (0, 0, 0)
        Sphere sphere = new Sphere(new Point(0, 0, 0), 5.0);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the surface of the sphere
        // Using a Pythagorean triple (0, 3, 4) for the point on the sphere surface.
        // The normal vector should be the normalized vector from the center to the point.
        Vector expectedNormal = new Vector(0, 3.0 / 5.0, 4.0 / 5.0);

        assertEquals(expectedNormal, sphere.getNormal(new Point(0, 3, 4)),
                "ERROR: getNormal() wrong result for a point on the sphere");
    }

    /**
     * Test method for {@link geometries.impl.Sphere#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray's line is outside the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(1, 1, 0))),
                "Ray's line out of sphere");

        // EP02: Ray starts before and crosses the sphere (2 points)
        Point p1 = new Point(0.0651530771650466, 0.355051025721682, 0);
        Point p2 = new Point(1.53484692283495, 0.844948974278318, 0);
        List<Point> result02 = SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(3, 1, 0)));

        assertNotNull(result02, "Wrong sphere intersection result");
        assertEquals(2, result02.size(), "Wrong number of points");
        assertEquals(List.of(p1, p2), result02, "Ray crosses sphere");

        // EP03: Ray starts inside the sphere (1 point)
        List<Point> result03 = SPHERE.findIntersections(new Ray(new Point(0.5, 0, 0), new Vector(1, 0, 0)));
        assertEquals(1, result03.size(), "Wrong number of points");

        // EP04: Ray starts after the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))),
                "Ray starts after sphere");

        // =============== Boundary Values Tests ==================
        // **** Group 1: Ray's line goes through the center
        // BV11: Ray starts before the sphere (2 points)
        List<Point> result11 = SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(1, 0, 0)));
        assertEquals(2, result11.size(), "Wrong number of points");

        // BV12: Ray starts at center (1 point)
        List<Point> result12 = SPHERE.findIntersections(new Ray(p100, new Vector(1, 0, 0)));
        assertEquals(1, result12.size(), "Wrong number of points");

        // **** Group 2: Ray's line is tangent to the sphere (all tests 0 points)
        // BV21: Ray starts before the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 1, 0), new Vector(1, 0, 0))),
                "Tangent line, ray starts before");
    }
}