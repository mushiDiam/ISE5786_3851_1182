package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link geometries.impl.Plane} class.
 */
class PlaneTests {

    /**
     * Default constructor for PlaneTests.
     */
    PlaneTests() {
    }

    /**
     * Test method for {@link geometries.impl.Plane#Plane(primitives.Point, primitives.Point, primitives.Point)}.
     */
    @Test
    void testConstructor() {
        Point p1 = new Point(0, 0, 1);
        Point p2 = new Point(1, 0, 0);
        Point p3 = new Point(0, 1, 0);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct plane constructor with 3 different non-collinear points
        assertDoesNotThrow(() -> new Plane(p1, p2, p3),
                "Failed constructing a correct plane");

        // =============== Boundary Values Tests ==================
        // BV01: Two points coincide
        assertThrows(IllegalArgumentException.class, () -> new Plane(p1, p1, p3),
                "Constructed a plane with two coinciding points");

        // BV02: Three points coincide
        assertThrows(IllegalArgumentException.class, () -> new Plane(p1, p1, p1),
                "Constructed a plane with three coinciding points");

        // BV03: Three points are collinear
        assertThrows(IllegalArgumentException.class, () -> new Plane(
                        new Point(1, 1, 1), new Point(2, 2, 2), new Point(3, 3, 3)),
                "Constructed a plane with three collinear points");
    }

    /**
     * Test method for {@link geometries.impl.Plane#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Points on the axes to easily calculate the normal (Z-axis direction)
        Point p1 = new Point(0, 0, 0);
        Point p2 = new Point(1, 0, 0);
        Point p3 = new Point(0, 1, 0);
        Plane plane = new Plane(p1, p2, p3);

        Vector expectedNormal = new Vector(0, 0, 1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the plane that is not the reference point (p1)
        assertEquals(expectedNormal, plane.getNormal(new Point(1, 1, 0)),
                "ERROR: getNormal() wrong result for a general point on the plane");

        // EP02: Test that Plane(Point, Vector) constructor normalizes the given vector
        Plane planeWithVector = new Plane(new Point(0, 0, 0), new Vector(0, 0, 5));
        assertEquals(expectedNormal, planeWithVector.getNormal(new Point(0, 0, 0)),
                "ERROR: Plane constructor doesn't normalize the normal vector");

        // =============== Boundary Values Tests ==================
        // BV01: Point is exactly the reference point (q) of the plane
        assertEquals(expectedNormal, plane.getNormal(p1),
                "ERROR: getNormal() wrong result for the reference point");
    }

    /**
     * Test method for {@link geometries.impl.Plane#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        Plane plane = new Plane(new Point(1, 0, 0), new Vector(0, 1, 0));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects the plane (1 point)
        List<Point> result01 = plane.findIntersections(new Ray(new Point(1, -1, 0), new Vector(0, 1, 1)));
        assertEquals(1, result01.size(), "Wrong number of points");
        assertEquals(new Point(1, 0, 1), result01.get(0), "Ray crosses plane");

        // EP02: Ray does not intersect the plane (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(1, 2, 0), new Vector(0, 1, 1))),
                "Ray does not cross plane");

        // =============== Boundary Values Tests ==================
        // **** Group 1: Ray is parallel to the plane
        // BV11: Ray included in the plane
        assertNull(plane.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 0, 1))),
                "Ray is included in the plane");
        // BV12: Ray not included in the plane
        assertNull(plane.findIntersections(new Ray(new Point(1, 1, 0), new Vector(1, 0, 1))),
                "Ray is parallel and outside the plane");

        // **** Group 2: Ray is orthogonal to the plane
        // BV21: Ray starts before the plane
        List<Point> result21 = plane.findIntersections(new Ray(new Point(1, -1, 0), new Vector(0, 1, 0)));
        assertEquals(1, result21.size(), "Wrong number of points");
        // BV22: Ray starts in the plane
        assertNull(plane.findIntersections(new Ray(new Point(1, 0, 0), new Vector(0, 1, 0))),
                "Ray starts in the plane");
        // BV23: Ray starts after the plane
        assertNull(plane.findIntersections(new Ray(new Point(1, 1, 0), new Vector(0, 1, 0))),
                "Ray starts after the plane");

        // **** Group 3: Special cases
        // BV31: Ray begins at the plane (but not orthogonal or parallel)
        assertNull(plane.findIntersections(new Ray(new Point(2, 0, 0), new Vector(1, 1, 1))),
                "Ray starts at the plane");
        // BV32: Ray begins at the exact point that defines the plane
        assertNull(plane.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 1, 1))),
                "Ray starts at the reference point of the plane");
    }
}