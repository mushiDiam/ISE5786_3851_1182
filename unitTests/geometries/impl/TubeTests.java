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
 * Unit tests for {@link geometries.impl.Tube} class.
 */
class TubeTests {

    /**
     * Default constructor for TubeTests.
     */
    TubeTests() {
    }

    /**
     * Radius of the tube used for tests.
     */
    private static final double RADIUS = 2d;

    /**
     * Axis ray of the tube used for tests.
     */
    private static final Ray AXIS = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));

    /**
     * Tube instance used for intersection tests.
     */
    private static final Tube TUBE = new Tube(RADIUS, AXIS);

    /**
     * Test method for {@link geometries.impl.Tube#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Tube with radius 1.0, axis starting at (0,0,0) and going along the Z-axis
        Tube tube = new Tube(1.0, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the tube surface in front of the axis ray (t > 0)
        assertEquals(new Vector(1, 0, 0), tube.getNormal(new Point(1, 0, 1)),
                "ERROR: getNormal() wrong result for a point in front of the ray");

        // EP02: Point on the tube surface behind the axis ray (t < 0)
        assertEquals(new Vector(1, 0, 0), tube.getNormal(new Point(1, 0, -1)),
                "ERROR: getNormal() wrong result for a point behind the ray");

        // =============== Boundary Values Tests ==================
        // BV01: Point on the tube surface exactly opposite the ray's head (t = 0)
        assertEquals(new Vector(1, 0, 0), tube.getNormal(new Point(1, 0, 0)),
                "ERROR: getNormal() wrong result for a point exactly opposite the ray's head");
    }

    /**
     * Test method for {@link geometries.impl.Tube#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses the tube side-to-side (orthogonal) (2 points)
        List<Point> resultEP01 = TUBE.findIntersections(new Ray(new Point(-4, 0, 1), new Vector(1, 0, 0)));
        assertNotNull(resultEP01, "Expected intersections");
        assertEquals(2, resultEP01.size(), "Wrong number of points");
        assertEquals(List.of(new Point(-2, 0, 1), new Point(2, 0, 1)), resultEP01, "Orthogonal cross");

        // EP02: Ray crosses the tube (acute angle to axis) (2 points)
        List<Point> resultEP02 = TUBE.findIntersections(new Ray(new Point(-4, 0, 0), new Vector(1, 0, 1)));
        assertNotNull(resultEP02, "Expected intersections");
        assertEquals(2, resultEP02.size(), "Wrong number of points");
        assertEquals(List.of(new Point(-2, 0, 2), new Point(2, 0, 6)), resultEP02, "Acute cross");

        // EP03: Ray completely misses the tube (skew line) (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(0, 1, 1))),
                "Skew line outside tube");

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the tube's axis
        // BV11: Ray is parallel and strictly inside the tube (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 0), new Vector(0, 0, 1))),
                "Parallel ray inside tube");

        // BV12: Ray is parallel and strictly outside the tube (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(3, 0, 0), new Vector(0, 0, 1))),
                "Parallel ray outside tube");

        // BV13: Ray is parallel and lies exactly on the tube's surface (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 0), new Vector(0, 0, 1))),
                "Parallel ray on tube surface");

        // **** Group 2: Ray is orthogonal to the tube's axis
        // BV21: Ray starts exactly on the axis (1 point)
        List<Point> resultBV21 = TUBE.findIntersections(new Ray(new Point(0, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultBV21, "Expected intersections");
        assertEquals(1, resultBV21.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 2)), resultBV21, "Starts on axis orthogonal");

        // BV22: Ray starts strictly inside the tube, not on axis (1 point)
        List<Point> resultBV22 = TUBE.findIntersections(new Ray(new Point(1, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultBV22, "Expected intersections");
        assertEquals(1, resultBV22.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 2)), resultBV22, "Starts inside orthogonal");

        // BV23: Ray starts on the surface and goes inside (1 point)
        List<Point> resultBV23 = TUBE.findIntersections(new Ray(new Point(-2, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultBV23, "Expected intersections");
        assertEquals(1, resultBV23.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 2)), resultBV23, "Starts on surface goes inside orthogonal");

        // BV24: Ray starts on the surface and goes outside (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 2), new Vector(1, 0, 0))),
                "Starts on surface goes outside orthogonal");

        // BV25: Ray is tangent to the tube (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(2, -2, 2), new Vector(0, 1, 0))),
                "Tangent orthogonal ray");

        // **** Group 3: Ray intersects the axis (not orthogonal, not parallel)
        // BV31: Ray starts exactly on the axis (1 point)
        List<Point> resultBV31 = TUBE.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 1)));
        assertNotNull(resultBV31, "Expected intersections");
        assertEquals(1, resultBV31.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 2)), resultBV31, "Starts on axis angled");

        // BV32: Ray starts before the tube and passes through the axis (2 points)
        List<Point> resultBV32 = TUBE.findIntersections(new Ray(new Point(-4, 0, -4), new Vector(1, 0, 1)));
        assertNotNull(resultBV32, "Expected intersections");
        assertEquals(2, resultBV32.size(), "Wrong number of points");
        assertEquals(List.of(new Point(-2, 0, -2), new Point(2, 0, 2)), resultBV32, "Passes through axis angled");

        // **** Group 4: Tangent rays (not orthogonal)
        // BV41: Ray is tangent to the tube at an angle (0 points)
        assertNull(TUBE.findIntersections(new Ray(new Point(2, -2, 0), new Vector(0, 1, 1))),
                "Tangent angled ray");
    }

    /**
     * Test method for {@link geometries.api.Intersectable#calcIntersections(Ray, double)}.
     * Tests the Bonus functionality (Stage 8) ensuring intersections are properly filtered
     * by the maxDistance parameter.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        // Ray crosses the tube side-to-side (orthogonal).
        // Intersects at (-2, 0, 1) [distance 2] and (2, 0, 1) [distance 6].
        Ray ray = new Ray(new Point(-4, 0, 1), new Vector(1, 0, 0));

        // TC01: maxDistance is smaller than the first intersection (0 points)
        assertNull(TUBE.calcIntersections(ray, 1), "Expected no intersections because maxDistance is too small");

        // TC02: maxDistance is exactly the first intersection (1 point)
        assertEquals(1, TUBE.calcIntersections(ray, 2).size(), "Expected 1 intersection at the maxDistance boundary");

        // TC03: maxDistance is between the two intersections (1 point)
        assertEquals(1, TUBE.calcIntersections(ray, 4).size(), "Expected 1 intersection since second is too far");

        // TC04: maxDistance is exactly the second intersection (2 points)
        assertEquals(2, TUBE.calcIntersections(ray, 6).size(), "Expected 2 intersections at the maxDistance boundary");

        // TC05: maxDistance is larger than the second intersection (2 points)
        assertEquals(2, TUBE.calcIntersections(ray, 7).size(), "Expected 2 intersections since both are within maxDistance");

    }
}