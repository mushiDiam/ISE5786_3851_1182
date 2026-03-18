package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}