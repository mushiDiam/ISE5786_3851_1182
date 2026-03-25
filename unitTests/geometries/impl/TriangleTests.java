package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link geometries.impl.Triangle} class.
 */
class TriangleTests {

    /**
     * Test method for {@link geometries.impl.Triangle#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Create a triangle on the X-Y plane (Z=0)
        // Using the right-hand rule, the normal should be pointing towards the positive Z-axis
        Triangle triangle = new Triangle(
                new Point(0, 0, 0),
                new Point(1, 0, 0),
                new Point(0, 1, 0)
        );

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point inside the triangle
        Vector expectedNormal = new Vector(0, 0, 1);

        assertEquals(expectedNormal, triangle.getNormal(new Point(0.25, 0.25, 0)),
                "ERROR: getNormal() wrong result for a point inside the triangle");
    }

    /**
     * Test method for {@link geometries.impl.Triangle#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        Triangle triangle = new Triangle(new Point(0, 1, 0), new Point(2, 0, 0), new Point(-2, 0, 0));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects inside the triangle
        List<Point> result01 = triangle.findIntersections(new Ray(new Point(0, 0.5, -1), new Vector(0, 0, 1)));
        assertEquals(1, result01.size(), "Wrong number of points");
        assertEquals(new Point(0, 0.5, 0), result01.get(0), "Ray crosses inside triangle");

        // EP02: Ray outside against edge
        assertNull(triangle.findIntersections(new Ray(new Point(0, 2, -1), new Vector(0, 0, 1))),
                "Ray outside against edge");

        // EP03: Ray outside against vertex
        assertNull(triangle.findIntersections(new Ray(new Point(3, 0, -1), new Vector(0, 0, 1))),
                "Ray outside against vertex");

        // =============== Boundary Values Tests ==================
        // BV01: Ray intersects on edge
        assertNull(triangle.findIntersections(new Ray(new Point(1, 0.5, -1), new Vector(0, 0, 1))),
                "Ray intersects on edge");

        // BV02: Ray intersects in vertex
        assertNull(triangle.findIntersections(new Ray(new Point(0, 1, -1), new Vector(0, 0, 1))),
                "Ray intersects on vertex");

        // BV03: Ray intersects on edge's continuation
        assertNull(triangle.findIntersections(new Ray(new Point(3, -0.5, -1), new Vector(0, 0, 1))),
                "Ray intersects on edge's continuation");
    }
}