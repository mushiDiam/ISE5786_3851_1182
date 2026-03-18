package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}