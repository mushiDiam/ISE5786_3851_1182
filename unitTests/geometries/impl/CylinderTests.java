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
 * Unit tests for the {@link Cylinder} class.
 */
class CylinderTests {

    /**
     * Constructs the cylinder test class.
     */
    CylinderTests() {
    }

    /**
     * Radius of the cylinder used for tests.
     */
    private static final double RADIUS = 2d;

    /**
     * Height of the cylinder used for tests.
     */
    private static final double HEIGHT = 4d;

    /**
     * Axis ray of the cylinder used for tests.
     */
    private static final Ray AXIS = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));

    /**
     * Cylinder instance used for intersection tests.
     */
    private static final Cylinder CYLINDER = new Cylinder(RADIUS, AXIS, HEIGHT);

    /**
     * Test method for {@link geometries.impl.Cylinder#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Cylinder with radius 1, centered at (0,0,0), directed along the Z-axis, height 2
        Cylinder cylinder = new Cylinder(1.0, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 2.0);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the side surface of the cylinder
        assertEquals(new Vector(1, 0, 0), cylinder.getNormal(new Point(1, 0, 1)),
                "ERROR: getNormal() wrong result for side surface");

        // EP02: Point on the top base of the cylinder
        assertEquals(new Vector(0, 0, 1), cylinder.getNormal(new Point(0.5, 0, 2)),
                "ERROR: getNormal() wrong result for top base");

        // EP03: Point on the bottom base of the cylinder
        assertEquals(new Vector(0, 0, -1), cylinder.getNormal(new Point(0.5, 0, 0)),
                "ERROR: getNormal() wrong result for bottom base");

        // =============== Boundary Values Tests ==================
        // BV01: Point at the center of the top base (to ensure no zero vector is created)
        assertEquals(new Vector(0, 0, 1), cylinder.getNormal(new Point(0, 0, 2)),
                "ERROR: getNormal() wrong result for center of top base");

        // BV02: Point at the center of the bottom base (to ensure no zero vector is created)
        assertEquals(new Vector(0, 0, -1), cylinder.getNormal(new Point(0, 0, 0)),
                "ERROR: getNormal() wrong result for center of bottom base");

        // BV03: Point on the edge between side and top base
        assertEquals(new Vector(0, 0, 1), cylinder.getNormal(new Point(1, 0, 2)),
                "ERROR: getNormal() wrong result for edge of top base");

        // BV04: Point on the edge between side and bottom base
        assertEquals(new Vector(0, 0, -1), cylinder.getNormal(new Point(1, 0, 0)),
                "ERROR: getNormal() wrong result for edge of bottom base");
    }

    /**
     * Test method for {@link geometries.impl.Cylinder#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses the side surface side-to-side (2 points)
        List<Point> resultEP01 = CYLINDER.findIntersections(new Ray(new Point(-4, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultEP01, "Expected intersections");
        assertEquals(2, resultEP01.size(), "Wrong number of points");
        assertEquals(List.of(new Point(-2, 0, 2), new Point(2, 0, 2)), resultEP01, "Side to side cross");

        // EP02: Ray crosses from bottom base to top base (2 points)
        List<Point> resultEP02 = CYLINDER.findIntersections(new Ray(new Point(0, 0, -1), new Vector(0, 0, 1)));
        assertNotNull(resultEP02, "Expected intersections");
        assertEquals(2, resultEP02.size(), "Wrong number of points");
        assertEquals(List.of(new Point(0, 0, 0), new Point(0, 0, 4)), resultEP02, "Base to base cross");

        // EP03: Ray crosses from bottom base to side surface (2 points)
        List<Point> resultEP03 = CYLINDER.findIntersections(new Ray(new Point(0, 0, -1), new Vector(1, 0, 1)));
        assertNotNull(resultEP03, "Expected intersections");
        assertEquals(2, resultEP03.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0, 0), new Point(2, 0, 1)), resultEP03, "Base to side cross");

        // EP04: Ray is completely outside the cylinder (0 points)
        assertNull(CYLINDER.findIntersections(new Ray(new Point(4, 0, 0), new Vector(0, 1, 1))),
                "Ray outside cylinder");

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray is parallel to the cylinder's axis
        // BV11: Ray parallel, strictly inside cylinder (2 points - crosses both bases)
        List<Point> resultBV11 = CYLINDER.findIntersections(new Ray(new Point(1, 0, -1), new Vector(0, 0, 1)));
        assertNotNull(resultBV11, "Expected intersections");
        assertEquals(2, resultBV11.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0, 0), new Point(1, 0, 4)), resultBV11, "Parallel inside");

        // BV12: Ray parallel, exactly on the side surface (0 points - tangent to the cylinder)
        // Per PDF, junction points are included UNLESS it's a tangent ("לא אם מדובר בהשקה").
        assertNull(CYLINDER.findIntersections(new Ray(new Point(2, 0, -1), new Vector(0, 0, 1))),
                "Parallel ray on surface is tangent and should return null");

        // BV13: Ray parallel, outside cylinder (0 points)
        assertNull(CYLINDER.findIntersections(new Ray(new Point(3, 0, -1), new Vector(0, 0, 1))),
                "Parallel outside");

        // **** Group 2: Ray is orthogonal to the cylinder's axis
        // BV21: Ray orthogonal, crosses side to side, strictly between bases (2 points)
        List<Point> resultBV21 = CYLINDER.findIntersections(new Ray(new Point(-4, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultBV21, "Expected intersections");
        assertEquals(2, resultBV21.size(), "Wrong number of points");

        // BV22: Ray orthogonal, exactly on the plane of the bottom base, crossing the base (0 points)
        // Ray on the base plane is considered tangent to the cylinder's 3D volume.
        assertNull(CYLINDER.findIntersections(new Ray(new Point(-4, 0, 0), new Vector(1, 0, 0))),
                "Orthogonal exactly on bottom base plane");

        // BV23: Ray orthogonal, completely above the top base (0 points)
        assertNull(CYLINDER.findIntersections(new Ray(new Point(-4, 0, 5), new Vector(1, 0, 0))),
                "Orthogonal above top base");

        // **** Group 3: Intersections exactly at the rims (junction of base and side)
        // BV31: Ray crosses exactly through the bottom and top rims (2 points)
        List<Point> resultBV31 = CYLINDER.findIntersections(new Ray(new Point(-4, 0, -2), new Vector(1, 0, 1)));
        assertNotNull(resultBV31, "Expected intersections");
        assertEquals(2, resultBV31.size(), "Wrong number of points");
        assertEquals(List.of(new Point(-2, 0, 0), new Point(2, 0, 4)), resultBV31, "Crosses through rims");

        // **** Group 4: Ray starts exactly on the cylinder
        // BV41: Ray starts exactly at the center of the bottom base and goes out the top base (1 point)
        List<Point> resultBV41 = CYLINDER.findIntersections(new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)));
        assertNotNull(resultBV41, "Expected intersections");
        assertEquals(1, resultBV41.size(), "Wrong number of points");
        assertEquals(List.of(new Point(0, 0, 4)), resultBV41, "Starts bottom center goes up");

        // BV42: Ray starts exactly on the side surface and goes strictly inside (1 point)
        List<Point> resultBV42 = CYLINDER.findIntersections(new Ray(new Point(-2, 0, 2), new Vector(1, 0, 0)));
        assertNotNull(resultBV42, "Expected intersections");
        assertEquals(1, resultBV42.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 2)), resultBV42, "Starts side surface goes in");

        // BV43: Ray starts exactly on the bottom base, going strictly inside (1 point)
        List<Point> resultBV43 = CYLINDER.findIntersections(new Ray(new Point(1, 0, 0), new Vector(0, 0, 1)));
        assertNotNull(resultBV43, "Expected intersections");
        assertEquals(1, resultBV43.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0, 4)), resultBV43, "Starts bottom base goes up");
    }

    /**
     * Test method for {@link geometries.api.Intersectable#calcIntersections(Ray, double)}.
     * Tests the Bonus functionality (Stage 8) ensuring intersections are properly filtered
     * by the maxDistance parameter.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        // Ray crosses from bottom base to top base
        // Intersections are at z=0 (distance=1) and z=4 (distance=5)
        Ray ray = new Ray(new Point(0, 0, -1), new Vector(0, 0, 1));

        // TC01: maxDistance is smaller than the first intersection (0 points)
        assertNull(CYLINDER.calcIntersections(ray, 0.5), "Expected no intersections because maxDistance is too small");

        // TC02: maxDistance is exactly the first intersection (1 point)
        assertEquals(1, CYLINDER.calcIntersections(ray, 1).size(), "Expected 1 intersection at the maxDistance boundary");

        // TC03: maxDistance is between the two intersections (1 point)
        assertEquals(1, CYLINDER.calcIntersections(ray, 3).size(), "Expected 1 intersection since second is too far");

        // TC04: maxDistance is exactly the second intersection (2 points)
        assertEquals(2, CYLINDER.calcIntersections(ray, 5).size(), "Expected 2 intersections at the maxDistance boundary");

        // TC05: maxDistance is larger than the second intersection (2 points)
        assertEquals(2, CYLINDER.calcIntersections(ray, 6).size(), "Expected 2 intersections since both are within maxDistance");
    }
}