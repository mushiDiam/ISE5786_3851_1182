package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.impl.Tube} class.
 */
class TubeTests {

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
        Tube tube = new Tube(1d, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses tube (2 points)
        List<Point> result01 = tube.findIntersections(new Ray(new Point(-2, 0, 0.5), new Vector(1, 0, 0)));
        assertNotNull(result01, "Wrong tube intersection");
        assertEquals(2, result01.size(), "Wrong number of points");

        // EP02: Ray outside tube and parallel to axis (0 points)
        assertNull(tube.findIntersections(new Ray(new Point(2, 0, 0), new Vector(0, 0, 1))),
                "Ray parallel outside tube");

        // EP03: Ray strictly inside tube and parallel to axis (0 points)
        assertNull(tube.findIntersections(new Ray(new Point(0.5, 0, 0), new Vector(0, 0, 1))),
                "Ray parallel inside tube");

        // =============== Boundary Values Tests ==================
        // BV01: Ray is tangent to the tube (0 points)
        assertNull(tube.findIntersections(new Ray(new Point(1, -1, 0), new Vector(0, 1, 0))),
                "Ray tangent to tube");

        // BV02: Ray originates on the surface and goes outside (0 points)
        assertNull(tube.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 0, 0))),
                "Ray starting on surface going outside");

        // BV03: Ray crosses the central axis (2 points)
        List<Point> result03 = tube.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result03, "Wrong tube intersection");
        assertEquals(2, result03.size(), "Ray crossing axis must yield 2 points");
    }
}