package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}