package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link geometries.impl.Plane} class.
 */
class PlaneTests {

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
}