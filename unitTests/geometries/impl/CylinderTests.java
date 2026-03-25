package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.impl.Cylinder} class.
 */
class CylinderTests {

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
        Cylinder cylinder = new Cylinder(1d, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 2d);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses the side surface twice
        List<Point> result01 = cylinder.findIntersections(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0)));
        assertNotNull(result01, "Wrong cylinder intersection");
        assertEquals(2, result01.size(), "Wrong number of points");

        // EP02: Ray crosses a base and the side surface
        List<Point> result02 = cylinder.findIntersections(
                new Ray(new Point(0.5, 0, -1), new Vector(0, 0.5, 1)));
        assertNotNull(result02, "Wrong cylinder intersection");
        assertEquals(2, result02.size(), "Wrong number of points");

        // EP03: Ray crosses both bases
        List<Point> result03 = cylinder.findIntersections(new Ray(new Point(0, 0.5, -1), new Vector(0, 0, 1)));
        assertNotNull(result03, "Wrong cylinder intersection");
        assertEquals(2, result03.size(), "Wrong number of points");

        // EP04: Ray outside the cylinder (0 points)
        assertNull(cylinder.findIntersections(new Ray(new Point(0, 0, 3), new Vector(1, 0, 0))),
                "Ray outside cylinder");

        // =============== Boundary Values Tests ==================
        // BV01: Ray passes through exactly the center of both bases
        List<Point> resultBV01 = cylinder.findIntersections(new Ray(new Point(0, 0, -1), new Vector(0, 0, 1)));
        assertNotNull(resultBV01, "Wrong cylinder intersection");
        assertEquals(2, resultBV01.size(), "Ray passing through centers");
    }

    @Test
    void testFindIntersections1() {
        Cylinder cylinder = new Cylinder(1d, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 2d);

        System.out.println("EP01: " + cylinder.findIntersections(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0))));
        System.out.println("EP02: " + cylinder.findIntersections(new Ray(new Point(0, 0.5, -1), new Vector(0, 1, 1))));
        System.out.println("EP03: " + cylinder.findIntersections(new Ray(new Point(0, 0.5, -1), new Vector(0, 0, 1))));
        System.out.println("EP04: " + cylinder.findIntersections(new Ray(new Point(0, 0, 3), new Vector(1, 0, 0))));
        System.out.println("BV01: " + cylinder.findIntersections(new Ray(new Point(0, 0, -1), new Vector(0, 0, 1))));
    }
}