package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link geometries.impl.Sphere} class.
 */
class SphereTests {

    /**
     * Default constructor for SphereTests.
     */
    SphereTests() {
    }

    /**
     * A predefined point on the X-axis used for tests.
     */
    private static final Point p100 = new Point(1, 0, 0);

    /**
     * Sphere instance used for intersection tests.
     */
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
        assertNotNull(result03, "Wrong sphere intersection result");
        assertEquals(1, result03.size(), "Wrong number of points");

        // EP04: Ray starts after the sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))),
                "Ray starts after sphere");

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray's line crosses the sphere (but not the center)
        // BV1: Ray starts at sphere and goes inside (1 point)
        List<Point> result11 = SPHERE.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 1, 0)));
        assertNotNull(result11, "Wrong sphere intersection result");
        assertEquals(1, result11.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 1, 0)), result11, "Ray crosses sphere from surface inside");

        // BV2: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 0, 0), new Vector(-1, -1, 0))),
                "Ray starts at sphere and goes outside");

        // **** Group 2: Ray's line goes through the center
        // BV21: Ray starts before the sphere (2 points)
        List<Point> result21 = SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result21, "Wrong sphere intersection result");
        assertEquals(2, result21.size(), "Wrong number of points");
        assertEquals(List.of(new Point(0, 0, 0), new Point(2, 0, 0)), result21, "Ray through center");

        // BV22: Ray starts at sphere and goes inside (1 point)
        List<Point> result22 = SPHERE.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result22, "Wrong sphere intersection result");
        assertEquals(1, result22.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result22, "Ray from surface through center");

        // BV23: Ray starts inside (1 point)
        List<Point> result23 = SPHERE.findIntersections(new Ray(new Point(0.5, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result23, "Wrong sphere intersection result");
        assertEquals(1, result23.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result23, "Ray from inside through center");

        // BV24: Ray starts at the center (1 point)
        List<Point> result24 = SPHERE.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result24, "Wrong sphere intersection result");
        assertEquals(1, result24.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result24, "Ray from center");

        // BV25: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 0, 0), new Vector(1, 0, 0))),
                "Ray from surface through center outside");

        // BV26: Ray starts after sphere (0 points)
        assertNull(SPHERE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))),
                "Ray after sphere through center line");

        // **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)
        // BV31: Ray starts before the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 1, 0), new Vector(1, 0, 0))),
                "Tangent line, ray starts before");

        // BV32: Ray starts at the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(1, 1, 0), new Vector(1, 0, 0))),
                "Tangent line, ray starts at tangent point");

        // BV33: Ray starts after the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 1, 0), new Vector(1, 0, 0))),
                "Tangent line, ray starts after");

        // **** Group 4: Special cases
        // BV41: Ray's line is outside sphere, ray is orthogonal to ray start to sphere's center line
        assertNull(SPHERE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(0, 0, 1))),
                "Ray orthogonal to start-to-center line, outside");

        // BV42: Ray starts inside, ray is orthogonal to ray start to sphere's center line
        List<Point> result42 = SPHERE.findIntersections(new Ray(new Point(1.5, 0, 0), new Vector(0, 0, 1)));
        assertNotNull(result42, "Wrong sphere intersection result");
        assertEquals(1, result42.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1.5, 0, Math.sqrt(0.75))), result42, "Ray orthogonal to start-to-center line, inside");
    }
}